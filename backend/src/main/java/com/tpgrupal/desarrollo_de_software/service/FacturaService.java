package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.auth.SesionActual;
import com.tpgrupal.desarrollo_de_software.dto.*;
import com.tpgrupal.desarrollo_de_software.exception.DuplicateResourceException;
import com.tpgrupal.desarrollo_de_software.exception.FacturaConCaeException;
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

    @org.springframework.beans.factory.annotation.Autowired
    private ReporteService reporteService;

    @Transactional(readOnly = true)
    public byte[] generarPdfFactura(Long idOrNumero) {
        return reporteService.generarPdfFactura(buscarFacturaPorIdONumero(idOrNumero));
    }

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

    // Columnas por las que se puede ordenar el listado (se arma con una lista cerrada
    // para no meter en la consulta texto que venga del navegador)
    private static final Map<String, String> ORDENES = Map.of(
            "fecha_desc", "f.fechaEmision DESC, f.numero DESC",
            "fecha_asc", "f.fechaEmision ASC, f.numero ASC",
            "numero_desc", "f.numero DESC",
            "numero_asc", "f.numero ASC",
            "importe_desc", "f.importeTotal DESC, f.numero DESC",
            "importe_asc", "f.importeTotal ASC, f.numero ASC",
            "cliente_asc", "COALESCE(c.denominacion, 'Consumidor Final') ASC, f.numero DESC",
            "cliente_desc", "COALESCE(c.denominacion, 'Consumidor Final') DESC, f.numero DESC"
    );
    private static final String ORDEN_POR_DEFECTO = "fecha_desc";

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
            String orden,
            int page,
            int size) {

        if (size < 1) size = 5;
        if (page < 0) page = 0;

        Map<String, Object> params = new HashMap<>();
        String filtros = armarFiltros(numero, cliente, condicionIva, fechaDesde, fechaHasta,
                estado, montoMinimo, listaPrecioId, params);

        // 1. COUNT: cuántas facturas cumplen los filtros (sin traerlas)
        TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(f) " + FROM_LISTADO + filtros, Long.class);
        params.forEach(countQuery::setParameter);
        long totalElements = countQuery.getSingleResult();

        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages - 1) page = totalPages - 1;

        // 2. Se trae de la base solo la página pedida
        TypedQuery<FacturaReporteDTO> query = em.createQuery(
                SELECT_LISTADO + FROM_LISTADO + filtros + "ORDER BY " + ordenJpql(orden),
                FacturaReporteDTO.class);
        params.forEach(query::setParameter);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<FacturaReporteDTO> content = query.getResultList();

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

    // Lista completa (sin paginar) para exportar a PDF / Excel
    @Transactional(readOnly = true)
    public List<FacturaReporteDTO> buscarFacturasFiltradas(
            Long numero,
            String cliente,
            String condicionIva,
            Date fechaDesde,
            Date fechaHasta,
            String estado,
            Double montoMinimo,
            Long listaPrecioId,
            String orden) {

        Map<String, Object> params = new HashMap<>();
        String filtros = armarFiltros(numero, cliente, condicionIva, fechaDesde, fechaHasta,
                estado, montoMinimo, listaPrecioId, params);

        TypedQuery<FacturaReporteDTO> query = em.createQuery(
                SELECT_LISTADO + FROM_LISTADO + filtros + "ORDER BY " + ordenJpql(orden),
                FacturaReporteDTO.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    // Una fila por factura. La cantidad de ítems sale de una subconsulta,
    // así no hace falta unir los detalles ni agrupar (GROUP BY).
    private static final String SELECT_LISTADO =
            "SELECT new com.tpgrupal.desarrollo_de_software.dto.FacturaReporteDTO(" +
            "  f.id, f.numero, f.fechaEmision, COALESCE(c.denominacion, 'Consumidor Final'), " +
            "  ci.denominacion, pv.descripcion, f.importeTotal, " +
            "  (SELECT COUNT(d) FROM FacturaVentaDetalle d WHERE d.factura = f), f.estado, f.cae" +
            ") ";

    private static final String FROM_LISTADO =
            "FROM FacturaVenta f " +
            "LEFT JOIN f.cliente c " +
            "JOIN f.condicionIva ci " +
            "JOIN f.puntoVenta pv " +
            "WHERE 1=1 ";

    private String ordenJpql(String orden) {
        String clave = orden != null ? orden.trim().toLowerCase() : ORDEN_POR_DEFECTO;
        return ORDENES.getOrDefault(clave, ORDENES.get(ORDEN_POR_DEFECTO)) + ", f.id DESC";
    }

    // Arma el WHERE con los filtros que vinieron (lo usan el COUNT y la consulta de la página)
    private String armarFiltros(
            Long numero,
            String cliente,
            String condicionIva,
            Date fechaDesde,
            Date fechaHasta,
            String estado,
            Double montoMinimo,
            Long listaPrecioId,
            Map<String, Object> params) {

        StringBuilder jpql = new StringBuilder();

        if (numero != null) {
            jpql.append("AND f.numero = :numero ");
            params.put("numero", numero);
        }
        if (cliente != null && !cliente.trim().isEmpty()) {
            jpql.append("AND LOWER(COALESCE(c.denominacion, 'Consumidor Final')) LIKE LOWER(:cliente) ");
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
            jpql.append("AND EXISTS (SELECT d FROM FacturaVentaDetalle d " +
                    "WHERE d.factura = f AND d.listaPrecioArticulo.listaPrecio.id = :listaPrecioId) ");
            params.put("listaPrecioId", listaPrecioId);
        }
        return jpql.toString();
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
        validarDetalles(dto);
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
        
        f.setPuntoVenta(resolverPuntoVenta(dto.getPuntoVentaId()));
        f.setCondicionIva(resolverCondicionIva(dto.getCondicionIvaId()));
        f.setCliente(resolverClienteFactura(dto.getClienteId(), f.getCondicionIva()));

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

        // Una factura informada a ARCA (con CAE) ya no se puede editar
        if (tieneCae(f)) {
            throw new FacturaConCaeException("La factura " + f.getNumero()
                    + " ya fue informada a ARCA (CAE " + f.getCae() + ") y no se puede editar. Solo se puede anular.");
        }

        // Si vienen los ítems es una edición completa desde el formulario
        boolean edicionCompleta = dto.getDetalles() != null && !dto.getDetalles().isEmpty();
        if (edicionCompleta) {
            validarDetalles(dto);
        }

        if (dto.getPuntoVentaId() != null) {
            f.setPuntoVenta(resolverPuntoVenta(dto.getPuntoVentaId()));
        }
        if (dto.getCondicionIvaId() != null) {
            f.setCondicionIva(resolverCondicionIva(dto.getCondicionIvaId()));
        }
        if (edicionCompleta || dto.getClienteId() != null) {
            f.setCliente(resolverClienteFactura(dto.getClienteId(), f.getCondicionIva()));
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
        if (tieneCae(f) && !"ANULADA".equals(estadoUpper)) {
            throw new FacturaConCaeException("La factura " + f.getNumero()
                    + " ya fue informada a ARCA: solo se puede anular (baja lógica).");
        }
        f.setEstado(estadoUpper);
        if ("ANULADA".equals(estadoUpper)) {
            // Baja lógica: la factura queda guardada pero marcada como dada de baja
            Date ahora = new Date();
            if (f.getFechaAnulacion() == null) f.setFechaAnulacion(ahora);
            if (f.getFechaBaja() == null) f.setFechaBaja(ahora);
            if (f.getUsuarioBaja() == null) f.setUsuarioBaja(resolverUsuarioAuditoria());
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

    // Eliminación física: solo si la factura todavía NO fue informada a ARCA
    public void eliminarFactura(Long idOrNumero) {
        FacturaVenta f = buscarFacturaPorIdONumero(idOrNumero);
        if (tieneCae(f)) {
            throw new FacturaConCaeException("La factura " + f.getNumero()
                    + " ya fue informada a ARCA (tiene CAE) y no se puede eliminar. Solo se puede anular (baja lógica).");
        }
        facturaVentaRepository.delete(f);
    }

    private boolean tieneCae(FacturaVenta f) {
        return f.getCae() != null && !f.getCae().isBlank();
    }

    @Transactional(readOnly = true)
    public OpcionesComboDTO obtenerOpcionesCombo() {
        List<PuntoVenta> pvs = puntoVentaRepository.findAll();
        List<CondicionIva> cis = condicionIvaRepository.findAll();
        List<Cliente> cls = clienteRepository.findAll();

        List<OpcionesComboDTO.ItemComboDTO> pvsDTO = pvs.stream()
                .map(p -> new OpcionesComboDTO.ItemComboDTO(p.getId(), p.getNumero() + " - " + p.getDescripcion()))
                .toList();

        List<OpcionesComboDTO.ItemComboDTO> cisDTO = cis.stream()
                .map(c -> new OpcionesComboDTO.ItemComboDTO(c.getId(), c.getDenominacion()))
                .toList();

        List<OpcionesComboDTO.ItemComboDTO> clsDTO = cls.stream()
                .map(c -> new OpcionesComboDTO.ItemComboDTO(c.getId(), c.getDenominacion()))
                .toList();

        return OpcionesComboDTO.builder()
                .puntosVenta(pvsDTO)
                .condicionesIva(cisDTO)
                .clientes(clsDTO)
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
        // Si el ítem trae su propio precio de lista (listaPrecioArticuloId), se usa ese:
        // así cada ítem de la factura puede venir de una lista de precios distinta.
        ListaPrecioArticulo lpa = detDto.getListaPrecioArticuloId() != null
                ? listaPrecioArticuloRepository.findById(detDto.getListaPrecioArticuloId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el precio de lista con id: " + detDto.getListaPrecioArticuloId()))
                : resolverListaPrecioArticulo(detDto.getArticuloId(), listaPrecioId);
        
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
        Long listaPrecioId = null;
        if (f.getDetalles() != null) {
            for (FacturaVentaDetalle d : f.getDetalles()) {
                detallesDTO.add(FacturaDetalleReporteDTO.builder()
                        .id(d.getId())
                        .articuloId(d.getListaPrecioArticulo() != null && d.getListaPrecioArticulo().getArticulo() != null
                                ? d.getListaPrecioArticulo().getArticulo().getId() : null)
                        .listaPrecioArticuloId(d.getListaPrecioArticulo() != null ? d.getListaPrecioArticulo().getId() : null)
                        .listaPrecioId(d.getListaPrecioArticulo() != null && d.getListaPrecioArticulo().getListaPrecio() != null
                                ? d.getListaPrecioArticulo().getListaPrecio().getId() : null)
                        .productoDescripcion(d.getDescripcion())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getImporteSubtotal())
                        .build());
                
                // La lista de la factura es la de sus ítems
                if (listaPrecioId == null && d.getListaPrecioArticulo() != null
                        && d.getListaPrecioArticulo().getListaPrecio() != null) {
                    listaPrecioId = d.getListaPrecioArticulo().getListaPrecio().getId();
                }
            }
        }
        
        return FacturaReporteDTO.builder()
                .id(f.getId())
                .numeroFactura(f.getNumero())
                .fechaEmision(f.getFechaEmision())
                .clienteId(f.getCliente() != null ? f.getCliente().getId() : null)
                .clienteDenominacion(f.getCliente() != null ? f.getCliente().getDenominacion() : "Consumidor Final")
                .condicionIva(f.getCondicionIva() != null ? f.getCondicionIva().getDenominacion() : "")
                .puntoVentaDescripcion(f.getPuntoVenta() != null ? f.getPuntoVenta().getDescripcion() : "")
                .importeTotal(f.getImporteTotal())
                .cantidadItems(f.getDetalles() != null ? f.getDetalles().size() : 0)
                .estado(f.getEstado())
                .observaciones(f.getObservaciones())
                .listaPrecioId(listaPrecioId)
                .detalles(detallesDTO)
                .cae(f.getCae())
                .caeFechaVencimiento(f.getCaeFechaVencimiento())
                .resultadoAfip(f.getResultadoAfip())
                .nroComprobanteAfip(f.getNroComprobanteAfip())
                .tipoComprobanteAfip(f.getTipoComprobanteAfip())
                .build();
    }
    
    // Una factura debe tener al menos un producto, y cada ítem un producto elegido
    private void validarDetalles(FacturaRequestDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un producto.");
        }
        for (FacturaDetalleRequestDTO d : dto.getDetalles()) {
            if (d.getArticuloId() == null && d.getListaPrecioArticuloId() == null) {
                throw new IllegalArgumentException("Todos los ítems de la factura deben tener un producto seleccionado.");
            }
        }
    }

    // Sin cliente solo se permite si la factura es a Consumidor Final
    private Cliente resolverClienteFactura(Long clienteId, CondicionIva condicionIva) {
        if (clienteId != null) return resolverCliente(clienteId);
        if (esConsumidorFinal(condicionIva)) return null;
        throw new IllegalArgumentException("Debe seleccionar un cliente. Solo las facturas a Consumidor Final pueden ir sin cliente.");
    }

    private boolean esConsumidorFinal(CondicionIva ci) {
        if (ci == null) return false;
        if (ci.getCodigoAfip() == 5) return true;
        return ci.getDenominacion() != null && ci.getDenominacion().toLowerCase().contains("consumidor");
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
        // Si hay una sesión iniciada, la factura queda a nombre de ese usuario
        Long usuarioSesionId = SesionActual.getUsuarioId();
        if (usuarioSesionId != null) {
            Usuario usuarioSesion = em.find(Usuario.class, usuarioSesionId);
            if (usuarioSesion != null) return usuarioSesion;
        }

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
