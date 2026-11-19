package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.dto.ClienteDTO;
import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioArticuloDTO;
import com.tpgrupal.desarrollo_de_software.dto.PageResponse;
import com.tpgrupal.desarrollo_de_software.model.Cliente;
import com.tpgrupal.desarrollo_de_software.model.ListaPrecioArticulo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Búsquedas paginadas de los exploradores (productos y clientes): primero un COUNT con los filtros
 * y después solo la página pedida (LIMIT / OFFSET en la consulta). No distinguen mayúsculas ni tildes.
 */
@Service
@Transactional(readOnly = true)
public class BusquedaCatalogoService {

    private static final int TAMANIO_MAXIMO = 100;

    @PersistenceContext
    private EntityManager em;

    /**
     * Productos de las listas de precios. Cada palabra buscada tiene que aparecer en el código,
     * la denominación, el rubro, la marca o el nombre de la lista.
     */
    public PageResponse<ListaPrecioArticuloDTO> buscarProductos(String buscar, Long listaPrecioId, int pagina, int tamanio) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (listaPrecioId != null) {
            where.append("AND lp.id = :listaPrecioId ");
            params.put("listaPrecioId", listaPrecioId);
        }
        int i = 0;
        for (String palabra : palabras(buscar)) {
            String p = ":p" + i;
            where.append("AND (").append(sinTildesJpql("a.codigo")).append(" LIKE ").append(p)
                    .append(" OR ").append(sinTildesJpql("a.denominacion")).append(" LIKE ").append(p)
                    .append(" OR ").append(sinTildesJpql("COALESCE(r.denominacion, '')")).append(" LIKE ").append(p)
                    .append(" OR ").append(sinTildesJpql("COALESCE(m.denominacion, '')")).append(" LIKE ").append(p)
                    .append(" OR ").append(sinTildesJpql("lp.denominacion")).append(" LIKE ").append(p).append(") ");
            params.put("p" + i, "%" + palabra + "%");
            i++;
        }
        String from = "FROM ListaPrecioArticulo lpa JOIN lpa.articulo a JOIN lpa.listaPrecio lp "
                + "LEFT JOIN a.rubro r LEFT JOIN a.marca m ";
        return paginar("SELECT COUNT(lpa) " + from + where,
                "SELECT lpa " + from + where + "ORDER BY a.codigo, lp.denominacion, lpa.id",
                ListaPrecioArticulo.class, params, pagina, tamanio, BusquedaCatalogoService::aProductoDTO);
    }

    /** Clientes por CUIT/CUIL (con o sin guiones) y/o razón social. */
    public PageResponse<ClienteDTO> buscarClientes(String cuit, String razonSocial, int pagina, int tamanio) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        String digitos = cuit == null ? "" : cuit.replaceAll("\\D", "");
        if (!digitos.isEmpty()) {
            where.append("AND REPLACE(COALESCE(c.cuitCuil, ''), '-', '') LIKE :cuit ");
            params.put("cuit", "%" + digitos + "%");
        }
        if (razonSocial != null && !razonSocial.isBlank()) {
            where.append("AND ").append(sinTildesJpql("c.denominacion")).append(" LIKE :razon ");
            params.put("razon", "%" + sinTildes(razonSocial.trim().toLowerCase(Locale.ROOT)) + "%");
        }
        return paginar("SELECT COUNT(c) FROM Cliente c " + where,
                "SELECT c FROM Cliente c " + where + "ORDER BY c.denominacion, c.id",
                Cliente.class, params, pagina, tamanio, BusquedaCatalogoService::aClienteDTO);
    }

    /** Un solo texto: busca por razón social o por CUIT (lo usa el buscador de clientes de las pantallas). */
    public List<ClienteDTO> buscarClientesPorTexto(String texto) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (texto != null && !texto.isBlank()) {
            where.append("AND (").append(sinTildesJpql("c.denominacion")).append(" LIKE :razon ");
            params.put("razon", "%" + sinTildes(texto.trim().toLowerCase(Locale.ROOT)) + "%");
            String digitos = texto.replaceAll("\\D", "");
            if (!digitos.isEmpty()) {
                where.append("OR REPLACE(COALESCE(c.cuitCuil, ''), '-', '') LIKE :cuit ");
                params.put("cuit", "%" + digitos + "%");
            }
            where.append(") ");
        }
        TypedQuery<Cliente> q = em.createQuery("SELECT c FROM Cliente c " + where + "ORDER BY c.denominacion, c.id",
                Cliente.class);
        params.forEach(q::setParameter);
        return q.getResultList().stream().map(BusquedaCatalogoService::aClienteDTO).toList();
    }

    private <T, R> PageResponse<R> paginar(String jpqlConteo, String jpqlPagina, Class<T> tipo,
                                           Map<String, Object> params, int pagina, int tamanio,
                                           Function<T, R> mapper) {
        int tamanioPagina = Math.min(Math.max(tamanio, 1), TAMANIO_MAXIMO);
        TypedQuery<Long> conteo = em.createQuery(jpqlConteo, Long.class);
        params.forEach(conteo::setParameter);
        long total = conteo.getSingleResult();

        int totalPaginas = total == 0 ? 1 : (int) Math.ceil(total / (double) tamanioPagina);
        int paginaActual = Math.min(Math.max(pagina, 0), totalPaginas - 1);

        // Solo la página pedida: LIMIT = filas por página, OFFSET = filas de las páginas anteriores
        TypedQuery<T> query = em.createQuery(jpqlPagina + " LIMIT :limite OFFSET :desde", tipo);
        params.forEach(query::setParameter);
        query.setParameter("limite", tamanioPagina);
        query.setParameter("desde", paginaActual * tamanioPagina);
        List<R> contenido = query.getResultList().stream().map(mapper).toList();

        return PageResponse.<R>builder()
                .content(contenido)
                .pageNumber(paginaActual)
                .pageSize(tamanioPagina)
                .totalElements(total)
                .totalPages(totalPaginas)
                .first(paginaActual == 0)
                .last(paginaActual >= totalPaginas - 1)
                .build();
    }

    private static ListaPrecioArticuloDTO aProductoDTO(ListaPrecioArticulo lpa) {
        return ListaPrecioArticuloDTO.builder()
                .id(lpa.getId())
                .articuloId(lpa.getArticulo() != null ? lpa.getArticulo().getId() : null)
                .codigo(lpa.getArticulo() != null ? lpa.getArticulo().getCodigo() : "")
                .denominacion(lpa.getArticulo() != null ? lpa.getArticulo().getDenominacion() : "")
                .rubro(lpa.getArticulo() != null && lpa.getArticulo().getRubro() != null
                        ? lpa.getArticulo().getRubro().getDenominacion() : "")
                .precioVenta(lpa.getPrecioVenta())
                .listaPrecioNombre(lpa.getListaPrecio() != null ? lpa.getListaPrecio().getDenominacion() : "")
                .listaPrecioId(lpa.getListaPrecio() != null ? lpa.getListaPrecio().getId() : null)
                .marca(lpa.getArticulo() != null && lpa.getArticulo().getMarca() != null
                        ? lpa.getArticulo().getMarca().getDenominacion() : null)
                .build();
    }

    private static ClienteDTO aClienteDTO(Cliente c) {
        return ClienteDTO.builder()
                .id(c.getId())
                .cuitCuil(c.getCuitCuil())
                .denominacion(c.getDenominacion())
                .email(c.getContacto() != null ? c.getContacto().getEmail() : "-")
                .telefono(c.getContacto() != null ? c.getContacto().getTelefono() : "-")
                .build();
    }

    private static String[] palabras(String buscar) {
        if (buscar == null || buscar.isBlank()) return new String[0];
        return sinTildes(buscar.trim().toLowerCase(Locale.ROOT)).split("\\s+");
    }

    // LOWER y sin tildes en JPQL, para que "computacion" encuentre "Computación"
    private static String sinTildesJpql(String expresion) {
        String e = "LOWER(" + expresion + ")";
        for (String[] par : new String[][]{{"á", "a"}, {"é", "e"}, {"í", "i"}, {"ó", "o"}, {"ú", "u"}}) {
            e = "REPLACE(" + e + ", '" + par[0] + "', '" + par[1] + "')";
        }
        return e;
    }

    private static String sinTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
