package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.dto.*;
import com.tpgrupal.desarrollo_de_software.exception.DuplicateResourceException;
import com.tpgrupal.desarrollo_de_software.exception.ResourceNotFoundException;
import com.tpgrupal.desarrollo_de_software.model.*;
import com.tpgrupal.desarrollo_de_software.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacturaService {

    @PersistenceContext
    private EntityManager em;

    private final FacturaVentaRepository facturaVentaRepository;
    private final ClienteRepository clienteRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final CondicionIvaRepository condicionIvaRepository;
    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;

    public FacturaService(FacturaVentaRepository facturaVentaRepository,
                          ClienteRepository clienteRepository,
                          PuntoVentaRepository puntoVentaRepository,
                          CondicionIvaRepository condicionIvaRepository,
                          ListaPrecioArticuloRepository listaPrecioArticuloRepository) {
        this.facturaVentaRepository = facturaVentaRepository;
        this.clienteRepository = clienteRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.condicionIvaRepository = condicionIvaRepository;
        this.listaPrecioArticuloRepository = listaPrecioArticuloRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<FacturaReporteDTO> buscarFacturasPaginadas(
            Long numero,
            String cliente,
            String condicionIva,
            Date fechaDesde,
            Date fechaHasta,
            String estado,
            Double montoMinimo,
            Long listaPrecioId,
            int page,
            int size) {

        List<FacturaReporteDTO> todas = buscarFacturasFiltradas(
                numero, cliente, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId);

        int totalElements = todas.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = page * size;
        List<FacturaReporteDTO> content;
        if (fromIndex >= totalElements) {
            content = Collections.emptyList();
        } else {
            int toIndex = Math.min(fromIndex + size, totalElements);
            content = todas.subList(fromIndex, toIndex);
        }

        return PageResponse.<FacturaReporteDTO>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacturaReporteDTO> buscarFacturasFiltradas(
            Long numero,
            String cliente,
            String condicionIva,
            Date fechaDesde,
            Date fechaHasta,
            String estado,
            Double montoMinimo,
            Long listaPrecioId) {

        StringBuilder jpql = new StringBuilder(
                "SELECT new com.tpgrupal.desarrollo_de_software.dto.FacturaReporteDTO(" +
                        "  f.id, f.numero, f.fechaEmision, COALESCE(c.denominacion, 'Consumidor Final'), " +
                        "  ci.denominacion, pv.descripcion, f.importeTotal, COUNT(DISTINCT d.id), f.estado" +
                        ") " +
                        "FROM FacturaVenta f " +
                        "LEFT JOIN f.cliente c " +
                        "JOIN f.condicionIva ci " +
                        "JOIN f.puntoVenta pv " +
                        "LEFT JOIN f.detalles d " +
                        "WHERE 1=1 "
        );

        Map<String, Object> params = new HashMap<>();

        if (numero != null) {
            jpql.append("AND f.numero = :numero ");
            params.put("numero", numero);
        }
        if (cliente != null && !cliente.trim().isEmpty()) {
            jpql.append("AND (LOWER(c.denominacion) LIKE LOWER(:cliente) OR (:cliente = 'Consumidor Final' AND c IS NULL)) ");
            params.put("cliente", "%" + cliente.trim() + "%");
        }
        if (condicionIva != null && !condicionIva.trim().isEmpty()) {
            jpql.append("AND LOWER(ci.denominacion) LIKE LOWER(:condicionIva) ");
            params.put("condicionIva", "%" + condicionIva.trim() + "%");
        }
        if (fechaDesde != null) {
            jpql.append("AND f.fechaEmision >= :fechaDesde ");
            params.put("fechaDesde", fechaDesde);
        }
        if (fechaHasta != null) {
            jpql.append("AND f.fechaEmision <= :fechaHasta ");
            params.put("fechaHasta", fechaHasta);
        }
        if (estado != null && !estado.trim().isEmpty()) {
            jpql.append("AND f.estado = :estado ");
            params.put("estado", estado);
        }
        if (montoMinimo != null) {
            jpql.append("AND f.importeTotal >= :montoMinimo ");
            params.put("montoMinimo", montoMinimo);
        }
        if (listaPrecioId != null) {
            jpql.append("AND d.listaPrecioArticulo.listaPrecio.id = :listaPrecioId ");
            params.put("listaPrecioId", listaPrecioId);
        }

        jpql.append("GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, f.importeTotal, f.estado ");
        jpql.append("ORDER BY f.numero ASC");

        TypedQuery<FacturaReporteDTO> query = em.createQuery(jpql.toString(), FacturaReporteDTO.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public FacturaReporteDTO obtenerFacturaPorId(Long id) {
        FacturaVenta f = buscarFacturaPorIdONumero(id);
        return convertirAInformeDetallado(f);
    }

    @Transactional(readOnly = true)
    public FacturaRequestDTO obtenerFacturaPorNumero(Long numero) {
        FacturaVenta f = buscarFacturaPorIdONumero(numero);

        List<FacturaDetalleRequestDTO> detallesDto = f.getDetalles().stream()
                .map(d -> FacturaDetalleRequestDTO.builder()
                        .id(d.getId())
                        .articuloId(d.getListaPrecioArticulo() != null && d.getListaPrecioArticulo().getArticulo() != null
                                ? d.getListaPrecioArticulo().getArticulo().getId() : null)
                        .listaPrecioArticuloId(d.getListaPrecioArticulo() != null ? d.getListaPrecioArticulo().getId() : null)
                        .productoDescripcion(d.getDescripcion())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getImporteSubtotal())
                        .build())
                .collect(Collectors.toList());

        return FacturaRequestDTO.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .numeroFactura(f.getNumero())
                .fechaEmision(f.getFechaEmision())
                .clienteDenominacion(f.getCliente() != null ? f.getCliente().getDenominacion() : "Consumidor Final")
                .clienteId(f.getCliente() != null ? f.getCliente().getId() : null)
                .condicionIva(f.getCondicionIva() != null ? f.getCondicionIva().getDenominacion() : "")
                .condicionIvaId(f.getCondicionIva() != null ? f.getCondicionIva().getId() : null)
                .puntoVentaDescripcion(f.getPuntoVenta() != null ? f.getPuntoVenta().getDescripcion() : "")
                .puntoVentaId(f.getPuntoVenta() != null ? f.getPuntoVenta().getId() : null)
                .estado(f.getEstado())
                .observaciones(f.getObservaciones())
                .importeTotal(f.getImporteTotal())
                .detalles(detallesDto)
                .build();
    }

    public FacturaReporteDTO crearFactura(FacturaRequestDTO dto) {
        Date ahora = new Date();
        FacturaVenta f = new FacturaVenta();

        // Numero de factura
        Long num = dto.getNumeroFactura();
        if (num == null || num <= 0) {
            Long maxNum = facturaVentaRepository.findMaxNumero();
            num = (maxNum != null ? maxNum : 1000L) + 1L;
        }
        if (facturaVentaRepository.existsByNumero(num)) {
            throw new DuplicateResourceException("Ya existe una factura con el número " + num + ".");
        }
        f.setNumero(num);

        f.setFechaEmision(dto.getFechaEmision() != null ? dto.getFechaEmision() : ahora);
        f.setFechaAlta(ahora);

        String estado = (dto.getEstado() != null && !dto.getEstado().trim().isEmpty())
                ? dto.getEstado().trim().toUpperCase()
                : "EMITIDA";
        f.setEstado(estado);
        f.setObservaciones(dto.getObservaciones());
        
        f.setCliente(resolverCliente(dto.getClienteId()));
        f.setPuntoVenta(resolverPuntoVenta(dto.getPuntoVentaId()));
        f.setCondicionIva(resolverCondicionIva(dto.getCondicionIvaId()));

        f.setTipoMoneda(resolverTipoMoneda(dto.getTipoMonedaId()));

        Usuario usuarioAudit = resolverUsuarioAuditoria();
        f.setUsuarioCarga(usuarioAudit);
        f.setUsuarioModificacion(usuarioAudit);

        // Detalles
        double totalCalculado = 0.0;
        if (dto.getDetalles() != null) {
            for (FacturaDetalleRequestDTO detDto : dto.getDetalles()) {
                FacturaVentaDetalle det = crearDetalle(detDto, dto.getListaPrecioId());
                f.addDetalle(det);
                totalCalculado += det.getImporteSubtotal();
            }
        }

        f.setImporteTotal(totalCalculado > 0 ? totalCalculado : (dto.getImporteTotal() != null ? dto.getImporteTotal() : 0.0));
        f.setImporteCobrado(f.getImporteTotal());
        f.setImporteSaldo(0.0);

        facturaVentaRepository.save(f);
        em.flush();

        return convertirAInformeDetallado(f);
    }
    
    public FacturaReporteDTO actualizarFactura(Long id, FacturaRequestDTO dto) {
        FacturaVenta f = buscarFacturaPorIdONumero(id);
        
        if (dto.getClienteId() != null) {
            f.setCliente(resolverCliente(dto.getClienteId()));
        }
        if (dto.getPuntoVentaId() != null) {
            f.setPuntoVenta(resolverPuntoVenta(dto.getPuntoVentaId()));
        }
        if (dto.getCondicionIvaId() != null) {
            f.setCondicionIva(resolverCondicionIva(dto.getCondicionIvaId()));
        }
        if (dto.getTipoMonedaId() != null) {
            f.setTipoMoneda(resolverTipoMoneda(dto.getTipoMonedaId()));
        }
        if (dto.getFechaEmision() != null) {
            f.setFechaEmision(dto.getFechaEmision());
        }
        if (dto.getObservaciones() != null) {
            f.setObservaciones(dto.getObservaciones());
        }
        if (dto.getEstado() != null && !dto.getEstado().trim().isEmpty()) {
            String nuevoEstado = dto.getEstado().trim().toUpperCase();
            f.setEstado(nuevoEstado);
            if ("ANULADA".equals(nuevoEstado) && f.getFechaAnulacion() == null) {
                f.setFechaAnulacion(new Date());
            }
        }
        
        // Solo se reemplazan los ítems si el cliente los mandó
        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            f.getDetalles().clear();
            double total = 0.0;
            for (FacturaDetalleRequestDTO detDto : dto.getDetalles()) {
                FacturaVentaDetalle det = crearDetalle(detDto, dto.getListaPrecioId());
                f.addDetalle(det);
                total += det.getImporteSubtotal();
            }
            f.setImporteTotal(total);
            f.setImporteCobrado(total);
        }
        
        f.setFechaModificacion(new Date());
        facturaVentaRepository.save(f);
        em.flush();
        
        return convertirAInformeDetallado(f);
    }

    public FacturaReporteDTO actualizarEstado(Long idOrNumero, String estado, String observaciones) {
        FacturaVenta f = buscarFacturaPorIdONumero(idOrNumero);
        String estadoUpper = estado != null ? estado.trim().toUpperCase() : "EMITIDA";
        f.setEstado(estadoUpper);
        if ("ANULADA".equals(estadoUpper) && f.getFechaAnulacion() == null) {
            f.setFechaAnulacion(new Date());
        }
        if (observaciones != null) {
            f.setObservaciones(observaciones);
        }
        f.setFechaModificacion(new Date());
        facturaVentaRepository.save(f);
        em.flush();
        return convertirAInformeDetallado(f);
    }

    public void anularFactura(Long idOrNumero) {
        actualizarEstado(idOrNumero, "ANULADA", "Anulada por usuario");
    }

    public void eliminarFactura(Long idOrNumero) {
        FacturaVenta f = buscarFacturaPorIdONumero(idOrNumero);
        facturaVentaRepository.delete(f);
    }

    @Transactional(readOnly = true)
    public OpcionesComboDTO obtenerOpcionesCombo() {
        List<PuntoVenta> pvs = puntoVentaRepository.findAll();
        List<CondicionIva> cis = condicionIvaRepository.findAll();
        List<Cliente> cls = clienteRepository.findAll();
        List<ListaPrecioArticulo> lpas = listaPrecioArticuloRepository.findAll();

        List<OpcionesComboDTO.ItemComboDTO> pvsDTO = pvs.stream()
                .map(p -> new OpcionesComboDTO.ItemComboDTO(p.getId(), p.getNumero() + " - " + p.getDescripcion()))
                .toList();

        List<OpcionesComboDTO.ItemComboDTO> cisDTO = cis.stream()
                .map(c -> new OpcionesComboDTO.ItemComboDTO(c.getId(), c.getDenominacion()))
                .toList();

        List<OpcionesComboDTO.ItemComboDTO> clsDTO = cls.stream()
                .map(c -> new OpcionesComboDTO.ItemComboDTO(c.getId(), c.getDenominacion()))
                .toList();

        List<OpcionesComboDTO.ArticuloComboDTO> artsDTO = lpas.stream()
                .map(l -> new OpcionesComboDTO.ArticuloComboDTO(
                        l.getArticulo() != null ? l.getArticulo().getId() : l.getId(),
                        l.getId(),
                        l.getArticulo() != null ? l.getArticulo().getCodigo() : "ART",
                        l.getArticulo() != null ? l.getArticulo().getDenominacion() : "Articulo",
                        l.getPrecioVenta()))
                .toList();

        return OpcionesComboDTO.builder()
                .puntosVenta(pvsDTO)
                .condicionesIva(cisDTO)
                .clientes(clsDTO)
                .articulos(artsDTO)
                .build();
    }

    // --- Métodos de Ayuda Privados ---

    private FacturaVenta buscarFacturaPorIdONumero(Long idOrNumero) {
        if (idOrNumero == null) {
            throw new ResourceNotFoundException("ID o número de factura no proporcionado.");
        }
        return facturaVentaRepository.findById(idOrNumero)
                .or(() -> facturaVentaRepository.findByNumero(idOrNumero))
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con identificador: " + idOrNumero));
    }
    
    private static final double ALICUOTA_IVA = 1.21; // IVA 21% fijo
    
    private FacturaVentaDetalle crearDetalle(FacturaDetalleRequestDTO detDto, Long listaPrecioId) {
        ListaPrecioArticulo lpa = resolverListaPrecioArticulo(detDto.getArticuloId(), listaPrecioId);
        
        double cant = detDto.getCantidad() != null && detDto.getCantidad() > 0 ? detDto.getCantidad() : 1.0;
        double precio = lpa.getPrecioVenta();
        
        double subtotal = cant * precio;
        double neto = Math.round((subtotal / ALICUOTA_IVA) * 100.0) / 100.0;
        double iva = Math.round((subtotal - neto) * 100.0) / 100.0;
        
        FacturaVentaDetalle det = new FacturaVentaDetalle();
        det.setDescripcion(lpa.getArticulo().getDenominacion());
        det.setCantidad(cant);
        det.setPrecioUnitario(precio);
        det.setPorcentajeBonificacion(0.0);
        det.setImporteNeto(neto);
        det.setImporteIva(iva);
        det.setImporteSubtotal(subtotal);
        det.setListaPrecioArticulo(lpa);
        return det;
    }

    private FacturaReporteDTO convertirAInformeDetallado(FacturaVenta f) {
        List<FacturaDetalleReporteDTO> detallesDTO = new ArrayList<>();
        if (f.getDetalles() != null) {
            for (FacturaVentaDetalle d : f.getDetalles()) {
                detallesDTO.add(FacturaDetalleReporteDTO.builder()
                        .id(d.getId())
                        .articuloId(d.getListaPrecioArticulo() != null && d.getListaPrecioArticulo().getArticulo() != null
                                ? d.getListaPrecioArticulo().getArticulo().getId() : null)
                        .productoDescripcion(d.getDescripcion())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getImporteSubtotal())
                        .build());
            }
        }

        return FacturaReporteDTO.builder()
                .id(f.getId())
                .numeroFactura(f.getNumero())
                .fechaEmision(f.getFechaEmision())
                .clienteDenominacion(f.getCliente() != null ? f.getCliente().getDenominacion() : "Consumidor Final")
                .condicionIva(f.getCondicionIva() != null ? f.getCondicionIva().getDenominacion() : "")
                .puntoVentaDescripcion(f.getPuntoVenta() != null ? f.getPuntoVenta().getDescripcion() : "")
                .importeTotal(f.getImporteTotal())
                .cantidadItems(f.getDetalles() != null ? f.getDetalles().size() : 0)
                .estado(f.getEstado())
                .observaciones(f.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }
    
    private Cliente resolverCliente(Long id) {
        if (id == null) throw new IllegalArgumentException("clienteId es obligatorio.");
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe Cliente con id: " + id));
    }
    
    private PuntoVenta resolverPuntoVenta(Long id) {
        if (id == null) throw new IllegalArgumentException("puntoVentaId es obligatorio.");
        return puntoVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe Punto de Venta con id: " + id));
    }
    
    private CondicionIva resolverCondicionIva(Long id) {
        if (id == null) throw new IllegalArgumentException("condicionIvaId es obligatorio.");
        return condicionIvaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe Condición de IVA con id: " + id));
    }

    private PuntoVenta resolverPuntoVenta(Long id, String descripcionOId) {
        if (id != null) {
            return puntoVentaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No existe Punto de Venta con id: " + id));
        }
        if (descripcionOId != null) {
            try {
                int num = Integer.parseInt(descripcionOId.replaceAll("\\D+", ""));
                Optional<PuntoVenta> pv = puntoVentaRepository.findByNumero(num);
                if (pv.isPresent()) return pv.get();
            } catch (Exception ignored) {}
        }
        List<PuntoVenta> all = puntoVentaRepository.findAll();
        if (!all.isEmpty()) return all.get(0);
        throw new ResourceNotFoundException("No existen Puntos de Venta en la base de datos.");
    }

    private CondicionIva resolverCondicionIva(Long id, String denominacion) {
        if (id != null) {
            return condicionIvaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No existe Condición de IVA con id: " + id));
        }
        if (denominacion != null && !denominacion.trim().isEmpty()) {
            Optional<CondicionIva> ci = condicionIvaRepository.findByDenominacionIgnoreCase(denominacion.trim());
            if (ci.isPresent()) return ci.get();

            String trim = denominacion.trim().toLowerCase();
            List<CondicionIva> allCi = condicionIvaRepository.findAll();
            for (CondicionIva c : allCi) {
                String dLower = c.getDenominacion().toLowerCase();
                if (dLower.contains(trim) || trim.contains(dLower)
                        || (trim.contains("exen") && dLower.contains("exen"))
                        || (trim.contains("excep") && dLower.contains("exen"))) {
                    return c;
                }
            }
        }
        List<CondicionIva> all = condicionIvaRepository.findAll();
        if (!all.isEmpty()) return all.get(0);
        throw new ResourceNotFoundException("No existen Condiciones de IVA en la base de datos.");
    }
    
    private ListaPrecioArticulo resolverListaPrecioArticulo(Long articuloId, Long listaPrecioId) {
        if (articuloId == null) throw new IllegalArgumentException("Cada detalle requiere articuloId.");
        
        if (listaPrecioId != null) {
            return listaPrecioArticuloRepository.findByArticuloIdAndListaPrecioId(articuloId, listaPrecioId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El artículo " + articuloId + " no tiene precio en la lista " + listaPrecioId));
        }
        // Temporal: mientras el front no mande listaPrecioId, se usa el primer precio del artículo
        return listaPrecioArticuloRepository.findFirstByArticuloIdOrderByIdAsc(articuloId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe precio para el artículo " + articuloId));
    }

    private TipoMoneda resolverTipoMoneda(Long id) {
        if (id != null) {
            TipoMoneda tm = em.find(TipoMoneda.class, id);
            if (tm == null) throw new ResourceNotFoundException("No existe Tipo de Moneda con id: " + id);
            return tm;
        }
        List<TipoMoneda> monedas = em.createQuery("SELECT tm FROM TipoMoneda tm ORDER BY tm.id", TipoMoneda.class)
                .setMaxResults(1).getResultList();
        if (monedas.isEmpty()) throw new ResourceNotFoundException("No existen Tipos de Moneda en la base de datos.");
        return monedas.get(0);
    }

    private Usuario resolverUsuarioAuditoria() {
        List<Usuario> list = em.createQuery("SELECT u FROM Usuario u ORDER BY u.id ASC", Usuario.class)
                .setMaxResults(1)
                .getResultList();
        if (!list.isEmpty()) return list.get(0);

        Usuario u = new Usuario();
        u.setUsuario("admin");
        u.setClave("admin");
        u.setNombre("Admin");
        u.setApellido("Sistema");
        em.persist(u);
        return u;
    }
}
