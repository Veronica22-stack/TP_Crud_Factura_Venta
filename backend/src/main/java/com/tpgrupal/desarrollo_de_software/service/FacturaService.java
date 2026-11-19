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
import org.springframework.beans.factory.annotation.Value;
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
    private final FacturacionAfipService facturacionAfipService;

    // Si es true, al crear una factura se pide el CAE en el mismo momento
    @Value("${afip.auto-emitir:false}")
    private boolean autoEmitir;

    public FacturaService(FacturaVentaRepository facturaVentaRepository,
                          ClienteRepository clienteRepository,
                          PuntoVentaRepository puntoVentaRepository,
                          CondicionIvaRepository condicionIvaRepository,
                          ListaPrecioArticuloRepository listaPrecioArticuloRepository,
                          FacturacionAfipService facturacionAfipService) {
        this.facturaVentaRepository = facturaVentaRepository;
        this.clienteRepository = clienteRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.condicionIvaRepository = condicionIvaRepository;
        this.listaPrecioArticuloRepository = listaPrecioArticuloRepository;
        this.facturacionAfipService = facturacionAfipService;
    }

    // Columnas por las que se puede ordenar el listado (se arma con una lista cerrada
    // para no meter en la consulta texto que venga del navegador)
    private static final Map<String, String> ORDENES = new java.util.TreeMap<>(Map.of(
            "fecha_desc", "f.fechaEmision DESC, f.numero DESC",
            "fecha_asc", "f.fechaEmision ASC, f.numero ASC",
            "numero_desc", "f.numero DESC",
            "numero_asc", "f.numero ASC",
            "importe_desc", "f.importeTotal DESC, f.numero DESC",
            "importe_asc", "f.importeTotal ASC, f.numero ASC",
            "cliente_asc", "COALESCE(c.denominacion, 'Consumidor Final') ASC, f.numero DESC",
            "cliente_desc", "COALESCE(c.denominacion, 'Consumidor Final') DESC, f.numero DESC"
    ));
    private static final int TAMANIO_MAXIMO_PAGINA = 100;
    private static final String ORDEN_POR_DEFECTO = "fecha_desc";

    @Transactional(readOnly = true)
    public PageResponse<FacturaReporteDTO> buscarFacturasPaginadas(
            Long numero,
            String cliente,
            Long clienteId,
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
        if (size > TAMANIO_MAXIMO_PAGINA) size = TAMANIO_MAXIMO_PAGINA;
        if (page < 0) page = 0;
        String ordenarPor = ordenJpql(orden);

        Map<String, Object> params = new HashMap<>();
        String filtros = armarFiltros(numero, cliente, clienteId, condicionIva, fechaDesde, fechaHasta,
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
                SELECT_LISTADO + FROM_LISTADO + filtros + "ORDER BY " + ordenarPor,
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
            Long clienteId,
            String condicionIva,
            Date fechaDesde,
            Date fechaHasta,
            String estado,
            Double montoMinimo,
            Long listaPrecioId,
            String orden) {

        Map<String, Object> params = new HashMap<>();
        String filtros = armarFiltros(numero, cliente, clienteId, condicionIva, fechaDesde, fechaHasta,
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
            "  (SELECT COUNT(d) FROM FacturaVentaDetalle d WHERE d.factura = f), f.estado, f.cae, " +
            "  f.caeFechaVencimiento, f.resultadoAfip, f.motivoRechazo, f.tipoComprobante" +
            ") ";

    private static final String FROM_LISTADO =
            "FROM FacturaVenta f " +
            "LEFT JOIN f.cliente c " +
            "JOIN f.condicionIva ci " +
            "JOIN f.puntoVenta pv " +
            "WHERE 1=1 ";

    // Un orden que no está en la lista responde 400 (no se arma la consulta con texto del navegador)
    private String ordenJpql(String orden) {
        String clave = orden == null || orden.isBlank() ? ORDEN_POR_DEFECTO : orden.trim().toLowerCase(Locale.ROOT);
        String clausula = ORDENES.get(clave);
        if (clausula == null) {
            throw new IllegalArgumentException("Orden no válido: '" + orden + "'. Valores posibles: "
                    + String.join(", ", ORDENES.keySet()) + ".");
        }
        return clausula + ", f.id DESC";
    }

    // Arma el WHERE con los filtros que vinieron (lo usan el COUNT y la consulta de la página)
    private String armarFiltros(
            Long numero,
            String cliente,
            Long clienteId,
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
        // Cliente elegido en el buscador: filtro exacto por id
        if (clienteId != null) {
            jpql.append("AND c.id = :clienteId ");
            params.put("clienteId", clienteId);
        }
        // Cliente: parte del nombre o del CUIT (con o sin guiones). Sin cliente = "Consumidor Final".
        if (cliente != null && !cliente.trim().isEmpty()) {
            String digitos = cliente.replaceAll("\\D", "");
            jpql.append("AND (LOWER(COALESCE(c.denominacion, 'Consumidor Final')) LIKE LOWER(:cliente) ");
            if (!digitos.isEmpty()) {
                jpql.append("OR REPLACE(COALESCE(c.cuitCuil, ''), '-', '') LIKE :clienteCuit ");
                params.put("clienteCuit", "%" + digitos + "%");
            }
            jpql.append(") ");
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
            params.put("estado", estado.trim().toUpperCase(Locale.ROOT));
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
                        .porcentajeBonificacion(d.getPorcentajeBonificacion())
                        .importeNeto(d.getImporteNeto())
                        .importeIva(d.getImporteIva())
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
                .cbteTipo(f.getTipoComprobante())
                .tipoMonedaId(f.getTipoMoneda() != null ? f.getTipoMoneda().getId() : null)
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
        f.setTipoComprobante(resolverTipoComprobante(dto.getCbteTipo()));
        FacturacionAfipService.validarReceptor(f, f.getTipoComprobante());

        Usuario usuarioAudit = resolverUsuarioAuditoria();
        f.setUsuarioCarga(usuarioAudit);
        f.setUsuarioModificacion(usuarioAudit);

        // Detalles: el precio sale de la lista; subtotal, neto e IVA los calcula calcularTotales()
        for (FacturaDetalleRequestDTO detDto : dto.getDetalles()) {
            f.addDetalle(crearDetalle(detDto, dto.getListaPrecioId()));
        }
        f.calcularTotales();
        f.setImporteCobrado(f.getImporteTotal());
        f.setImporteSaldo(0.0);

        facturaVentaRepository.save(f);
        em.flush();

        // Opcional (afip.auto-emitir=true): se pide el CAE al guardar. Si falla, la factura queda guardada
        // sin CAE y con el motivo, para reintentar desde el listado.
        if (autoEmitir && facturacionAfipService.modo() != FacturacionAfipService.Modo.DESHABILITADO) {
            try {
                facturacionAfipService.emitir(f, null);
            } catch (RuntimeException e) {
                f.setMotivoRechazo(e.getMessage());
            }
            facturaVentaRepository.save(f);
            em.flush();
        }

        return convertirAInformeDetallado(f);
    }
    
    public FacturaReporteDTO actualizarFactura(Long id, FacturaRequestDTO dto) {
        FacturaVenta f = buscarFacturaPorIdONumero(id);

        // Una factura informada a ARCA (con CAE) ya no se puede editar: solo anularla (baja lógica),
        // con observaciones como motivo de la anulación
        if (tieneCae(f)) {
            boolean anulando = dto.getEstado() != null && "ANULADA".equalsIgnoreCase(dto.getEstado().trim());
            if (anulando) {
                return actualizarEstado(f.getId(), "ANULADA", dto.getObservaciones());
            }
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
        if (dto.getCbteTipo() != null) {
            f.setTipoComprobante(resolverTipoComprobante(dto.getCbteTipo()));
        }
        if (f.getTipoComprobante() != null) {
            FacturacionAfipService.validarReceptor(f, f.getTipoComprobante());
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
        if (edicionCompleta) {
            f.getDetalles().clear();
            for (FacturaDetalleRequestDTO detDto : dto.getDetalles()) {
                f.addDetalle(crearDetalle(detDto, dto.getListaPrecioId()));
            }
        }
        // Se recalcula siempre: cambiar el tipo de comprobante cambia el neto y el IVA de cada renglón
        f.calcularTotales();
        f.setImporteCobrado(f.getImporteTotal());
        f.setImporteSaldo(0.0);
        
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

    // Pide el CAE a ARCA de una factura guardada. El rechazo de ARCA no es un error HTTP:
    // la factura queda con resultadoAfip = R y el motivo, y se devuelve igual.
    public FacturaReporteDTO emitirCae(Long idOrNumero, Integer tipoComprobante) {
        FacturaVenta f = buscarFacturaPorIdONumero(idOrNumero);
        facturacionAfipService.emitir(f, tipoComprobante);
        facturaVentaRepository.save(f);
        em.flush();
        return convertirAInformeDetallado(f);
    }

    // Totales del panel de control, calculados en la base
    @Transactional(readOnly = true)
    public ResumenFacturacionDTO obtenerResumen(int cantidadUltimas) {
        Double total = em.createQuery(
                "SELECT COALESCE(SUM(f.importeTotal), 0) FROM FacturaVenta f WHERE f.estado <> 'ANULADA'",
                Double.class).getSingleResult();
        long vigentes = contar("f.estado <> 'ANULADA'");
        long conCae = contar("f.estado <> 'ANULADA' AND f.cae IS NOT NULL AND f.cae <> ''");
        long anuladas = contar("f.estado = 'ANULADA'");

        List<FacturaReporteDTO> ultimas = em.createQuery(
                        SELECT_LISTADO + FROM_LISTADO + "ORDER BY f.id DESC", FacturaReporteDTO.class)
                .setMaxResults(Math.min(Math.max(cantidadUltimas, 1), 20))
                .getResultList();

        return new ResumenFacturacionDTO(total == null ? 0 : total, vigentes, conCae, vigentes - conCae,
                anuladas, ultimas);
    }

    private long contar(String condicion) {
        return em.createQuery("SELECT COUNT(f) FROM FacturaVenta f WHERE " + condicion, Long.class)
                .getSingleResult();
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
    
    // 1 = Factura A, 6 = Factura B, 11 = Factura C (sin dato: el de afip.cbte.tipo)
    private int resolverTipoComprobante(Integer tipo) {
        int resultado = tipo != null && tipo > 0 ? tipo : facturacionAfipService.tipoComprobantePorDefecto();
        FacturacionAfipService.validarTipoComprobante(resultado);
        return resultado;
    }
    
    private FacturaVentaDetalle crearDetalle(FacturaDetalleRequestDTO detDto, Long listaPrecioId) {
        // Si el ítem trae su propio precio de lista (listaPrecioArticuloId), se usa ese:
        // así cada ítem de la factura puede venir de una lista de precios distinta.
        ListaPrecioArticulo lpa = detDto.getListaPrecioArticuloId() != null
                ? listaPrecioArticuloRepository.findById(detDto.getListaPrecioArticuloId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el precio de lista con id: " + detDto.getListaPrecioArticuloId()))
                : resolverListaPrecioArticulo(detDto.getArticuloId(), listaPrecioId);
        
        String nombre = lpa.getArticulo().getDenominacion();
        double cant = detDto.getCantidad() != null ? detDto.getCantidad() : 1.0;
        if (!(cant > 0) || Double.isInfinite(cant)) {
            throw new IllegalArgumentException("La cantidad de " + nombre + " debe ser mayor a cero.");
        }
        if (cant != Math.rint(cant)) {
            throw new IllegalArgumentException("La cantidad de " + nombre + " debe ser un número entero.");
        }
        double bonificacion = detDto.getPorcentajeBonificacion() != null ? detDto.getPorcentajeBonificacion() : 0.0;
        if (Double.isNaN(bonificacion) || bonificacion < 0 || bonificacion > 100) {
            throw new IllegalArgumentException("La bonificación de " + nombre + " debe estar entre 0 y 100.");
        }
        String descripcion = detDto.getDescripcion() != null && !detDto.getDescripcion().isBlank()
                ? detDto.getDescripcion().trim() : nombre;
        if (descripcion.length() > 255) {
            throw new IllegalArgumentException("La descripción de " + nombre + " no puede superar 255 caracteres.");
        }

        FacturaVentaDetalle det = new FacturaVentaDetalle();
        det.setDescripcion(descripcion);
        det.setCantidad(cant);
        det.setPrecioUnitario(lpa.getPrecioVenta()); // el precio lo pone el backend, desde la lista
        det.setPorcentajeBonificacion(bonificacion);
        det.setListaPrecioArticulo(lpa);
        return det;
    }
    
    private FacturaReporteDTO convertirAInformeDetallado(FacturaVenta f) {
        List<FacturaDetalleReporteDTO> detallesDTO = new ArrayList<>();
        double netoGravado = 0, ivaTotal = 0;
        if (f.getDetalles() != null) {
            for (FacturaVentaDetalle d : f.getDetalles()) {
                netoGravado += d.getImporteNeto();
                ivaTotal += d.getImporteIva();
            }
        }
        netoGravado = Math.round(netoGravado * 100.0) / 100.0;
        ivaTotal = Math.round(ivaTotal * 100.0) / 100.0;
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
                        .codigo(d.getListaPrecioArticulo() != null && d.getListaPrecioArticulo().getArticulo() != null
                                ? d.getListaPrecioArticulo().getArticulo().getCodigo() : null)
                        .porcentajeBonificacion(d.getPorcentajeBonificacion())
                        .importeNeto(d.getImporteNeto())
                        .importeIva(d.getImporteIva())
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
                .motivoRechazo(f.getMotivoRechazo())
                .tipoComprobante(f.getTipoComprobante())
                .clienteCuit(f.getCliente() != null ? f.getCliente().getCuitCuil() : null)
                .condicionIvaId(f.getCondicionIva() != null ? f.getCondicionIva().getId() : null)
                .puntoVentaId(f.getPuntoVenta() != null ? f.getPuntoVenta().getId() : null)
                .puntoVentaNumero(f.getPuntoVenta() != null ? f.getPuntoVenta().getNumero() : null)
                .tipoMonedaId(f.getTipoMoneda() != null ? f.getTipoMoneda().getId() : null)
                .monedaCodigoAfip(f.getTipoMoneda() != null ? f.getTipoMoneda().getCodigoAfip() : null)
                .monedaDenominacion(f.getTipoMoneda() != null ? f.getTipoMoneda().getDenominacion() : null)
                .importeNetoGravado(netoGravado)
                .importeIva(ivaTotal)
                .fechaAnulacion(f.getFechaAnulacion())
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
