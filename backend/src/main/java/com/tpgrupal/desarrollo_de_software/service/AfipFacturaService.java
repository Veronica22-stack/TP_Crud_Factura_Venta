package com.tpgrupal.desarrollo_de_software.service;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;

import com.tpgrupal.desarrollo_de_software.exception.AfipException;
import com.tpgrupal.desarrollo_de_software.util.AfipHttpUtil;

import ar.gov.afip.wsfe.AlicIva;
import ar.gov.afip.wsfe.ArrayOfAlicIva;
import ar.gov.afip.wsfe.ArrayOfErr;
import ar.gov.afip.wsfe.ArrayOfFECAEDetRequest;
import ar.gov.afip.wsfe.DummyResponse;
import ar.gov.afip.wsfe.Err;
import ar.gov.afip.wsfe.FEAuthRequest;
import ar.gov.afip.wsfe.FECAECabRequest;
import ar.gov.afip.wsfe.FECAEDetRequest;
import ar.gov.afip.wsfe.FECAERequest;
import ar.gov.afip.wsfe.FECAEResponse;
import ar.gov.afip.wsfe.FECotizacionResponse;
import ar.gov.afip.wsfe.FERecuperaLastCbteResponse;
import ar.gov.afip.wsfe.Moneda;
import ar.gov.afip.wsfe.MonedaResponse;
import ar.gov.afip.wsfe.Service;
import ar.gov.afip.wsfe.ServiceSoap;

/**
 * Cliente de WSFEv1 (facturación electrónica de ARCA) con los stubs JAX-WS generados (ar.gov.afip.wsfe).
 * Operaciones: FEDummy, FECompUltimoAutorizado, FECAESolicitar, FEParamGetTiposMonedas y FEParamGetCotizacion.
 */
public class AfipFacturaService {

    private static final int TIMEOUT_MS = 60000;
    private static final DateTimeFormatter AAAAMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Datos del comprobante que se manda a ARCA (salen de la factura guardada). */
    public record ComprobanteAfip(
            int puntoVenta,
            int tipoComprobante,      // 1 = A, 6 = B, 11 = C
            long numero,
            LocalDate fecha,
            int concepto,             // 1 = Productos
            int docTipo,              // 80 = CUIT, 99 = sin identificar
            long docNro,
            double importeTotal,
            double importeNeto,
            double importeIva,
            double alicuotaIva,       // en %, solo A y B
            String monedaId,          // MonId de ARCA (PES, DOL, 060...)
            double cotizacion,
            int condicionIvaReceptor  // RG 5616: 1 = RI, 4 = Exento, 5 = Consumidor Final, 6 = Monotributo
    ) {
        public boolean discriminaIva() {
            return tipoComprobante == 1 || tipoComprobante == 6;
        }
    }

    /** Estado de los servidores de ARCA (FEDummy). */
    public record EstadoServidores(String appServer, String dbServer, String authServer) {
        public boolean todoOk() {
            return "OK".equalsIgnoreCase(appServer) && "OK".equalsIgnoreCase(dbServer)
                    && "OK".equalsIgnoreCase(authServer);
        }
    }

    public record MonedaRemota(String codigoAfip, String denominacion) {
    }

    private AfipFacturaService() {
    }

    // ---------------------------------------------------------------------------------------------

    public static EstadoServidores dummy(boolean produccion) {
        try {
            DummyResponse r = puerto(produccion).feDummy();
            if (r == null) throw new AfipException("FEDummy devolvió una respuesta vacía.");
            return new EstadoServidores(r.getAppServer(), r.getDbServer(), r.getAuthServer());
        } catch (WebServiceException e) {
            throw new AfipException("No se pudo conectar con WSFEv1: " + mensaje(e), e);
        }
    }

    public static long ultimoComprobante(String token, String sign, long cuit, int puntoVenta, int tipo,
                                         boolean produccion) {
        try {
            FERecuperaLastCbteResponse r = puerto(produccion)
                    .feCompUltimoAutorizado(auth(token, sign, cuit), puntoVenta, tipo);
            if (r == null) throw new AfipException("FECompUltimoAutorizado devolvió una respuesta vacía.");
            List<String> errores = errores(r.getErrors());
            if (!errores.isEmpty()) {
                throw new AfipException("ARCA no informó el último comprobante: " + String.join(" | ", errores));
            }
            return r.getCbteNro();
        } catch (WebServiceException e) {
            throw new AfipException("No se pudo conectar con WSFEv1: " + mensaje(e), e);
        }
    }

    public static FECAEResponse solicitarCae(String token, String sign, long cuit, ComprobanteAfip c,
                                             boolean produccion) {
        FECAECabRequest cab = new FECAECabRequest();
        cab.setCantReg(1);
        cab.setPtoVta(c.puntoVenta());
        cab.setCbteTipo(c.tipoComprobante());

        FECAEDetRequest det = new FECAEDetRequest();
        det.setConcepto(c.concepto());
        det.setDocTipo(c.docTipo());
        det.setDocNro(c.docNro());
        det.setCbteDesde(c.numero());
        det.setCbteHasta(c.numero());
        String fecha = c.fecha().format(AAAAMMDD);
        det.setCbteFch(fecha);

        // Fechas obligatorias si se facturan servicios (Concepto 2 o 3)
        if (c.concepto() == 2 || c.concepto() == 3) {
            det.setFchServDesde(fecha);
            det.setFchServHasta(fecha);
            det.setFchVtoPago(fecha);
        }

        det.setMonId(c.monedaId());
        det.setMonCotiz(c.cotizacion());
        if (!"PES".equalsIgnoreCase(c.monedaId())) {
            det.setCanMisMonExt("N"); // se cobra en pesos (obligatorio para moneda extranjera)
        }
        det.setCondicionIVAReceptorId(c.condicionIvaReceptor());

        det.setImpTotal(c.importeTotal());
        det.setImpTotConc(0.00);
        det.setImpOpEx(0.00);
        det.setImpTrib(0.00);

        if (c.discriminaIva()) {
            // Factura A o B: neto gravado + IVA, con el nodo <Iva>
            det.setImpNeto(c.importeNeto());
            det.setImpIVA(c.importeIva());

            AlicIva alicIva = new AlicIva();
            alicIva.setId(idAlicuota(c.alicuotaIva()));
            alicIva.setBaseImp(c.importeNeto());
            alicIva.setImporte(c.importeIva());
            ArrayOfAlicIva arrayIva = new ArrayOfAlicIva();
            arrayIva.getAlicIva().add(alicIva);
            det.setIva(arrayIva);
        } else {
            // Factura C: no se discrimina IVA
            det.setImpNeto(c.importeTotal());
            det.setImpIVA(0.00);
        }

        ArrayOfFECAEDetRequest detArray = new ArrayOfFECAEDetRequest();
        detArray.getFECAEDetRequest().add(det);
        FECAERequest req = new FECAERequest();
        req.setFeCabReq(cab);
        req.setFeDetReq(detArray);

        try {
            FECAEResponse r = puerto(produccion).fecaeSolicitar(auth(token, sign, cuit), req);
            if (r == null) throw new AfipException("FECAESolicitar devolvió una respuesta vacía.");
            return r;
        } catch (WebServiceException e) {
            throw new AfipException("No se pudo conectar con WSFEv1: " + mensaje(e), e);
        }
    }

    public static List<MonedaRemota> tiposMonedas(String token, String sign, long cuit, boolean produccion) {
        try {
            MonedaResponse r = puerto(produccion).feParamGetTiposMonedas(auth(token, sign, cuit));
            if (r == null) throw new AfipException("FEParamGetTiposMonedas devolvió una respuesta vacía.");
            List<String> errores = errores(r.getErrors());
            if (!errores.isEmpty()) {
                throw new AfipException("ARCA no informó las monedas: " + String.join(" | ", errores));
            }
            List<MonedaRemota> monedas = new ArrayList<>();
            if (r.getResultGet() != null) {
                for (Moneda m : r.getResultGet().getMoneda()) {
                    monedas.add(new MonedaRemota(m.getId(), m.getDesc()));
                }
            }
            return monedas;
        } catch (WebServiceException e) {
            throw new AfipException("No se pudo conectar con WSFEv1: " + mensaje(e), e);
        }
    }

    /** Cotización de una moneda extranjera según ARCA (null si no la informa). */
    public static Double cotizacion(String token, String sign, long cuit, String monedaId, boolean produccion) {
        try {
            FECotizacionResponse r = puerto(produccion)
                    .feParamGetCotizacion(auth(token, sign, cuit), monedaId, null);
            if (r == null || r.getResultGet() == null || !errores(r.getErrors()).isEmpty()) {
                return null;
            }
            return r.getResultGet().getMonCotiz();
        } catch (WebServiceException e) {
            throw new AfipException("No se pudo conectar con WSFEv1: " + mensaje(e), e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    public static List<String> errores(ArrayOfErr errores) {
        List<String> lista = new ArrayList<>();
        if (errores != null && errores.getErr() != null) {
            for (Err e : errores.getErr()) {
                lista.add("[" + e.getCode() + "] " + e.getMsg());
            }
        }
        return lista;
    }

    private static ServiceSoap puerto(boolean produccion) {
        URL wsdl = AfipFacturaService.class.getResource("/wsdl/WSFEv1.wsdl");
        ServiceSoap port = new Service(wsdl).getServiceSoap();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, AfipHttpUtil.getURLWsfe(produccion));
        bp.getRequestContext().put("com.sun.xml.ws.connect.timeout", TIMEOUT_MS);
        bp.getRequestContext().put("com.sun.xml.ws.request.timeout", TIMEOUT_MS);
        return port;
    }

    private static FEAuthRequest auth(String token, String sign, long cuit) {
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(token.trim());
        auth.setSign(sign.trim());
        auth.setCuit(cuit);
        return auth;
    }

    // Id de alícuota de ARCA: 5 = 21%, 4 = 10.5%, 6 = 27%, 8 = 5%, 9 = 2.5%
    private static int idAlicuota(double alicuotaPct) {
        if (alicuotaPct == 10.5) return 4;
        if (alicuotaPct == 27.0) return 6;
        if (alicuotaPct == 5.0) return 8;
        if (alicuotaPct == 2.5) return 9;
        return 5;
    }

    private static String mensaje(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) causa = causa.getCause();
        return causa.getMessage() != null ? causa.getMessage() : e.toString();
    }
}
