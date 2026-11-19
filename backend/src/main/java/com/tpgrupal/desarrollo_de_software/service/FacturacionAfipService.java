package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.afip.MonedasAfip;
import com.tpgrupal.desarrollo_de_software.auth.SesionActual;
import com.tpgrupal.desarrollo_de_software.exception.AfipException;
import com.tpgrupal.desarrollo_de_software.exception.FacturaConCaeException;
import com.tpgrupal.desarrollo_de_software.model.Cliente;
import com.tpgrupal.desarrollo_de_software.model.FacturaVenta;
import com.tpgrupal.desarrollo_de_software.model.TipoMoneda;
import com.tpgrupal.desarrollo_de_software.model.Usuario;
import com.tpgrupal.desarrollo_de_software.repository.TipoMonedaRepository;
import com.tpgrupal.desarrollo_de_software.service.AfipFacturaService.ComprobanteAfip;
import com.tpgrupal.desarrollo_de_software.service.AfipFacturaService.EstadoServidores;
import com.tpgrupal.desarrollo_de_software.service.AfipFacturaService.MonedaRemota;
import com.tpgrupal.desarrollo_de_software.util.AfipHttpUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ar.gov.afip.wsfe.FECAEDetResponse;
import ar.gov.afip.wsfe.FECAEResponse;
import ar.gov.afip.wsfe.Obs;

/**
 * Integración con ARCA (ex AFIP): obtiene el CAE de una factura guardada.
 *
 * Modos (afip.modo):
 *  - DESHABILITADO: no se puede pedir CAE.
 *  - SIMULADO: genera un CAE ficticio sin conectarse a ARCA (para probar sin certificado ni internet).
 *  - HOMOLOGACION: ambiente de pruebas de ARCA (por defecto).
 *  - PRODUCCION: ambiente real.
 */
@Service
public class FacturacionAfipService {

    private static final Logger log = LoggerFactory.getLogger(FacturacionAfipService.class);
    private static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");
    // WSFEv1 acepta para productos (Concepto 1) fechas entre N-5 y N+5 días (error 10016)
    private static final int DIAS_TOLERANCIA_FECHA = 5;

    public enum Modo { DESHABILITADO, SIMULADO, HOMOLOGACION, PRODUCCION }

    @Value("${afip.modo:}")
    private String modoConfigurado;

    @Value("${afip.cuit:}")
    private String cuitConfigurado;

    @Value("${afip.key.path:}")
    private String keyPath;

    @Value("${afip.cert.path:}")
    private String certPath;

    @Value("${afip.keystore.path:}")
    private String keystorePath;

    @Value("${afip.keystore.password:}")
    private String keystorePassword;

    @Value("${afip.produccion:false}")
    private boolean produccionConfigurado;

    @Value("${afip.cbte.tipo:11}")
    private int cbteTipoDefault;

    @Value("${afip.alicuota.iva:21.0}")
    private double alicuotaDefault;

    @PersistenceContext
    private EntityManager em;

    private final TipoMonedaRepository tipoMonedaRepository;

    public FacturacionAfipService(TipoMonedaRepository tipoMonedaRepository) {
        this.tipoMonedaRepository = tipoMonedaRepository;
    }

    // ---------------------------------------------------------------------------------------------
    // Configuración

    /** Modo de trabajo. Si no se configuró afip.modo, se usa afip.produccion (como antes). */
    public Modo modo() {
        if (modoConfigurado != null && !modoConfigurado.isBlank()) {
            try {
                return Modo.valueOf(modoConfigurado.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                log.warn("afip.modo={} no es válido; se usa HOMOLOGACION", modoConfigurado);
            }
        }
        return produccionConfigurado ? Modo.PRODUCCION : Modo.HOMOLOGACION;
    }

    public boolean esProduccion() {
        return modo() == Modo.PRODUCCION;
    }

    public int tipoComprobantePorDefecto() {
        return cbteTipoDefault;
    }

    private String cuitNormalizado() {
        return cuitConfigurado == null ? "" : cuitConfigurado.replaceAll("\\D", "");
    }

    private boolean usaKeystore() {
        return keystorePath != null && !keystorePath.isBlank();
    }

    private boolean tieneCredenciales() {
        if (usaKeystore()) return new File(keystorePath).exists();
        return keyPath != null && !keyPath.isBlank() && certPath != null && !certPath.isBlank()
                && new File(keyPath).exists() && new File(certPath).exists();
    }

    private void validarConfiguracionReal() {
        if (!tieneCredenciales()) {
            throw new AfipException("Faltan las credenciales de ARCA: configurar afip.key.path + afip.cert.path "
                    + "(o afip.keystore.path) con archivos que existan, o usar afip.modo=SIMULADO.");
        }
        if (cuitNormalizado().length() != 11) {
            throw new AfipException("Configurar un CUIT válido de 11 dígitos en afip.cuit.");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Estado, diagnóstico y monedas

    public Map<String, Object> estado() {
        Modo modo = modo();
        String mensaje;
        if (modo == Modo.DESHABILITADO) {
            mensaje = "La integración con ARCA está deshabilitada (afip.modo=DESHABILITADO).";
        } else if (modo == Modo.SIMULADO) {
            mensaje = "Modo SIMULADO: se generan CAE ficticios sin conectarse a ARCA.";
        } else if (!tieneCredenciales()) {
            mensaje = "Modo " + modo + ": faltan las credenciales (certificado y clave, o keystore).";
        } else if (cuitNormalizado().length() != 11) {
            mensaje = "Modo " + modo + ": el CUIT configurado en afip.cuit no es válido.";
        } else {
            mensaje = "Modo " + modo + ": integración lista para operar contra ARCA.";
        }
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("modo", modo.name());
        estado.put("habilitado", modo != Modo.DESHABILITADO);
        estado.put("simulado", modo == Modo.SIMULADO);
        estado.put("cuit", cuitNormalizado());
        estado.put("credencialesConfiguradas", tieneCredenciales());
        estado.put("tipoComprobantePorDefecto", cbteTipoDefault);
        estado.put("alicuotaIva", alicuotaDefault);
        estado.put("wsaaUrl", AfipHttpUtil.getURLWsaa(esProduccion()));
        estado.put("wsfeUrl", AfipHttpUtil.getURLWsfe(esProduccion()));
        estado.put("mensaje", mensaje);
        return estado;
    }

    /** Prueba de conexión con WSFEv1 (FEDummy: no necesita ticket de acceso). */
    public Map<String, Object> probarConexion() {
        Modo modo = modo();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("modo", modo.name());
        if (modo == Modo.DESHABILITADO) {
            r.put("ok", false);
            r.put("mensaje", "La integración está deshabilitada.");
            return r;
        }
        if (modo == Modo.SIMULADO) {
            r.put("ok", true);
            r.put("servidores", new EstadoServidores("OK (simulado)", "OK (simulado)", "OK (simulado)"));
            r.put("mensaje", "Diagnóstico simulado: no se llamó a ARCA.");
            return r;
        }
        try {
            EstadoServidores servidores = AfipFacturaService.dummy(esProduccion());
            r.put("ok", servidores.todoOk());
            r.put("servidores", servidores);
            r.put("mensaje", servidores.todoOk()
                    ? "Conexión con WSFEv1 verificada correctamente."
                    : "ARCA respondió, pero algún servidor no está OK.");
        } catch (AfipException e) {
            r.put("ok", false);
            r.put("mensaje", e.getMessage());
        }
        return r;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> monedas() {
        return tipoMonedaRepository.findAll().stream()
                .sorted(Comparator.comparing(TipoMoneda::getCodigoAfip))
                .map(this::monedaComoMapa)
                .toList();
    }

    private Map<String, Object> monedaComoMapa(TipoMoneda tm) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", tm.getId());
        m.put("codigoAfip", tm.getCodigoAfip());
        m.put("codigoIso", MonedasAfip.codigoIso(tm.getCodigoAfip()));
        m.put("denominacion", tm.getDenominacion());
        m.put("simbolo", tm.getSimbolo());
        return m;
    }

    /** Actualiza el catálogo de monedas con FEParamGetTiposMonedas (solo HOMOLOGACION o PRODUCCION). */
    @Transactional
    public Map<String, Object> sincronizarMonedas() {
        Modo modo = modo();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("modo", modo.name());
        if (modo == Modo.DESHABILITADO || modo == Modo.SIMULADO) {
            r.put("sincronizado", false);
            r.put("mensaje", "La sincronización necesita afip.modo=HOMOLOGACION o PRODUCCION con credenciales. "
                    + "Se mantiene el catálogo local (" + tipoMonedaRepository.count() + " monedas).");
            r.put("monedas", monedas());
            return r;
        }
        validarConfiguracionReal();
        String[] auth = obtenerTokenYSign();
        List<MonedaRemota> remotas = AfipFacturaService.tiposMonedas(auth[0], auth[1], cuit(), esProduccion());

        Usuario usuario = usuarioAuditoria();
        Date ahora = new Date();
        int nuevas = 0, actualizadas = 0;
        for (MonedaRemota remota : remotas) {
            if (remota.codigoAfip() == null || remota.codigoAfip().isBlank()) continue;
            TipoMoneda moneda = tipoMonedaRepository.findFirstByCodigoAfipIgnoreCase(remota.codigoAfip()).orElse(null);
            if (moneda == null) {
                moneda = new TipoMoneda();
                moneda.setCodigoAfip(remota.codigoAfip().trim());
                moneda.setSimbolo(MonedasAfip.simbolo(MonedasAfip.codigoIso(remota.codigoAfip())));
                moneda.setUsuarioCarga(usuario);
                nuevas++;
            } else {
                actualizadas++;
            }
            moneda.setDenominacion(remota.denominacion() != null ? remota.denominacion() : remota.codigoAfip());
            moneda.setFechaModificacion(ahora);
            moneda.setUsuarioModificacion(usuario);
            tipoMonedaRepository.save(moneda);
        }
        r.put("sincronizado", true);
        r.put("mensaje", "Sincronizadas " + remotas.size() + " monedas desde ARCA (" + nuevas + " nuevas, "
                + actualizadas + " actualizadas).");
        r.put("nuevas", nuevas);
        r.put("actualizadas", actualizadas);
        r.put("monedas", monedas());
        return r;
    }

    // ---------------------------------------------------------------------------------------------
    // Emisión del CAE

    /**
     * Pide el CAE de una factura ya guardada y deja el resultado en la misma factura
     * (cae, vencimiento, número de ARCA o el motivo del rechazo). No la guarda: lo hace quien llama.
     *
     * - Datos fiscales inválidos (ARCA rechaza): no es un error, queda resultadoAfip = R y motivoRechazo.
     * - Configuración o red: AfipException (502).
     * - Factura con CAE, anulada o con fecha fuera de rango: excepción 409.
     */
    public void emitir(FacturaVenta factura, Integer tipoSolicitado) {
        Modo modo = modo();
        if (modo == Modo.DESHABILITADO) {
            throw new IllegalStateException("La integración con ARCA está deshabilitada (afip.modo=DESHABILITADO).");
        }
        if (factura.getCae() != null && !factura.getCae().isBlank()) {
            throw new FacturaConCaeException("La factura " + factura.getNumero() + " ya tiene CAE asignado: "
                    + factura.getCae());
        }
        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            throw new IllegalStateException("No se puede pedir el CAE de una factura anulada.");
        }
        if (factura.getDetalles() == null || factura.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura no tiene productos.");
        }
        validarFechaComprobante(factura);

        int tipo = resolverTipoComprobante(factura, tipoSolicitado);
        validarReceptor(factura, tipo);
        factura.setTipoComprobante(tipo);
        factura.calcularTotales();

        if (modo == Modo.SIMULADO) {
            simular(factura, tipo);
            return;
        }

        validarConfiguracionReal();
        String[] auth = obtenerTokenYSign();
        long cuit = cuit();
        int puntoVenta = factura.getPuntoVenta().getNumero();

        long numero = AfipFacturaService.ultimoComprobante(auth[0], auth[1], cuit, puntoVenta, tipo, esProduccion()) + 1;
        ComprobanteAfip comprobante = construirComprobante(factura, tipo, numero, auth);
        log.info("Solicitando CAE a ARCA ({}): ptoVta={} tipo={} nro={} total={} moneda={}",
                modo, puntoVenta, tipo, numero, comprobante.importeTotal(), comprobante.monedaId());

        FECAEResponse respuesta = AfipFacturaService.solicitarCae(auth[0], auth[1], cuit, comprobante, esProduccion());
        aplicarRespuesta(factura, respuesta, tipo);
    }

    public int resolverTipoComprobante(FacturaVenta factura, Integer solicitado) {
        int tipo;
        if (solicitado != null && solicitado > 0) tipo = solicitado;
        else if (factura.getTipoComprobante() != null && factura.getTipoComprobante() > 0) tipo = factura.getTipoComprobante();
        else tipo = cbteTipoDefault;
        validarTipoComprobante(tipo);
        return tipo;
    }

    public static void validarTipoComprobante(int tipo) {
        if (tipo != 1 && tipo != 6 && tipo != 11) {
            throw new IllegalArgumentException("Tipo de comprobante no válido: " + tipo
                    + ". Valores posibles: 1 (Factura A), 6 (Factura B) o 11 (Factura C).");
        }
    }

    /** La Factura A exige un cliente identificado con CUIT. */
    public static void validarReceptor(FacturaVenta factura, int tipo) {
        if (tipo != 1) return;
        Cliente cliente = factura.getCliente();
        String cuit = cliente == null || cliente.getCuitCuil() == null ? "" : cliente.getCuitCuil().replaceAll("\\D", "");
        if (cuit.length() != 11) {
            throw new IllegalArgumentException("La Factura A requiere un cliente con CUIT de 11 dígitos "
                    + "(no puede emitirse a Consumidor Final).");
        }
    }

    void validarFechaComprobante(FacturaVenta factura) {
        LocalDate fecha = fechaDe(factura);
        LocalDate hoy = LocalDate.now(ZONA_AR);
        LocalDate desde = hoy.minusDays(DIAS_TOLERANCIA_FECHA);
        LocalDate hasta = hoy.plusDays(DIAS_TOLERANCIA_FECHA);
        if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            throw new IllegalStateException("La fecha de emisión (" + fecha.format(f) + ") está fuera del rango que "
                    + "acepta ARCA: entre el " + desde.format(f) + " y el " + hasta.format(f)
                    + ". Modificar la fecha de la factura antes de pedir el CAE.");
        }
    }

    private ComprobanteAfip construirComprobante(FacturaVenta factura, int tipo, long numero, String[] auth) {
        Cliente cliente = factura.getCliente();
        String cuitCliente = cliente == null || cliente.getCuitCuil() == null
                ? "" : cliente.getCuitCuil().replaceAll("\\D", "");
        boolean tieneCuit = cuitCliente.length() == 11;

        String monedaId = monedaAfip(factura.getTipoMoneda());
        double cotizacion = resolverCotizacion(monedaId, auth);
        int condicionIvaReceptor = factura.getCondicionIva() == null ? 5 : factura.getCondicionIva().getCodigoAfip();

        return new ComprobanteAfip(
                factura.getPuntoVenta().getNumero(),
                tipo,
                numero,
                fechaDe(factura),
                1,
                tieneCuit ? 80 : 99,
                tieneCuit ? Long.parseLong(cuitCliente) : 0L,
                factura.getImporteTotal(),
                factura.getImporteNetoGravado(),
                factura.getImporteIvaTotal(),
                alicuotaDefault,
                monedaId,
                cotizacion,
                condicionIvaReceptor);
    }

    private static String monedaAfip(TipoMoneda moneda) {
        return moneda == null ? "PES" : MonedasAfip.codigoAfip(moneda.getCodigoAfip());
    }

    private double resolverCotizacion(String monedaId, String[] auth) {
        if (MonedasAfip.esPesos(monedaId)) return 1.0;
        Double cotizacion = null;
        try {
            cotizacion = AfipFacturaService.cotizacion(auth[0], auth[1], cuit(), monedaId, esProduccion());
        } catch (AfipException e) {
            log.warn("No se pudo obtener la cotización de {}: {}", monedaId, e.getMessage());
        }
        if (cotizacion == null || cotizacion <= 0) {
            throw new AfipException("ARCA no informó la cotización de la moneda " + monedaId
                    + ". Intentar más tarde o facturar en pesos.");
        }
        return cotizacion;
    }

    private void aplicarRespuesta(FacturaVenta factura, FECAEResponse respuesta, int tipo) {
        List<String> motivos = new ArrayList<>(AfipFacturaService.errores(respuesta.getErrors()));
        FECAEDetResponse det = respuesta.getFeDetResp() == null || respuesta.getFeDetResp().getFECAEDetResponse().isEmpty()
                ? null : respuesta.getFeDetResp().getFECAEDetResponse().get(0);
        if (det != null && det.getObservaciones() != null) {
            for (Obs o : det.getObservaciones().getObs()) {
                motivos.add("[" + o.getCode() + "] " + o.getMsg());
            }
        }

        factura.setFechaModificacion(new Date());
        if (det != null && "A".equals(det.getResultado()) && det.getCAE() != null && !det.getCAE().isBlank()) {
            factura.setResultadoAfip("A");
            factura.setCae(det.getCAE());
            factura.setCaeFechaVencimiento(fechaAfip(det.getCAEFchVto()));
            factura.setNroComprobanteAfip((int) det.getCbteDesde());
            factura.setTipoComprobanteAfip(tipo);
            factura.setMotivoRechazo(null);
            log.info("CAE obtenido: factura={} CAE={}", factura.getNumero(), det.getCAE());
        } else {
            factura.setResultadoAfip(det != null && det.getResultado() != null ? det.getResultado() : "R");
            factura.setMotivoRechazo(motivos.isEmpty() ? "ARCA no autorizó el comprobante." : String.join(" | ", motivos));
            log.warn("ARCA rechazó la factura {}: {}", factura.getNumero(), factura.getMotivoRechazo());
        }
    }

    private void simular(FacturaVenta factura, int tipo) {
        long numero = factura.getNumero() == null ? 1 : factura.getNumero();
        long base = Math.abs((System.currentTimeMillis() * 31 + numero * 7919) % 100_000_000_000_000L);
        factura.setCae(String.format("%014d", base));
        factura.setCaeFechaVencimiento(aDate(fechaDe(factura).plusDays(10)));
        factura.setResultadoAfip("A");
        factura.setNroComprobanteAfip((int) numero);
        factura.setTipoComprobanteAfip(tipo);
        factura.setMotivoRechazo(null);
        factura.setFechaModificacion(new Date());
        log.info("ARCA SIMULADO: factura={} tipo={} CAE={}", factura.getNumero(), tipo, factura.getCae());
    }

    private String[] obtenerTokenYSign() {
        try {
            if (usaKeystore()) {
                return AfipWsaaClient.obtenerTokenYSignP12(keystorePath, keystorePassword, esProduccion());
            }
            return AfipWsaaClient.obtenerTokenYSign(keyPath, certPath, esProduccion());
        } catch (AfipException e) {
            throw e;
        } catch (Exception e) {
            throw new AfipException("No se pudo obtener el ticket de acceso de WSAA: " + e.getMessage(), e);
        }
    }

    private long cuit() {
        return Long.parseLong(cuitNormalizado());
    }

    // Las fechas llegan del navegador como "yyyy-MM-dd" en UTC (ver FacturaRequestDTO): se leen en UTC
    private static LocalDate fechaDe(FacturaVenta factura) {
        if (factura.getFechaEmision() == null) return LocalDate.now(ZONA_AR);
        return factura.getFechaEmision().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private static Date fechaAfip(String aaaammdd) {
        if (aaaammdd == null || aaaammdd.length() != 8) return null;
        return aDate(LocalDate.parse(aaaammdd, DateTimeFormatter.BASIC_ISO_DATE));
    }

    private Usuario usuarioAuditoria() {
        Long id = SesionActual.getUsuarioId();
        if (id != null) {
            Usuario u = em.find(Usuario.class, id);
            if (u != null) return u;
        }
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                .setMaxResults(1).getResultStream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay usuarios cargados en la base."));
    }
}
