package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.dto.FacturaRequestDTO;
import com.tpgrupal.desarrollo_de_software.exception.FacturaConCaeException;
import com.tpgrupal.desarrollo_de_software.model.FacturaVenta;
import com.tpgrupal.desarrollo_de_software.repository.CondicionIvaRepository;
import com.tpgrupal.desarrollo_de_software.repository.FacturaVentaRepository;
import com.tpgrupal.desarrollo_de_software.repository.PuntoVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.gov.afip.wsfe.FECAEDetResponse;
import ar.gov.afip.wsfe.FECAEResponse;
import ar.gov.afip.wsfe.Obs;

@Service
public class FacturacionAfipService {

    @Value("${afip.cuit}")
    private long cuitEmisor;

    @Value("${afip.service:wsfe}")
    private String servicio;

    @Value("${afip.key.path}")
    private String keyPath;

    @Value("${afip.cert.path}")
    private String certPath;

    @Value("${afip.keystore.path:}")
    private String keystorePath;

    @Value("${afip.keystore.password:}")
    private String keystorePassword;

    @Value("${afip.produccion:false}")
    private boolean isProduccion;

    @Value("${afip.cbte.tipo:11}")
    private int cbteTipoDefault;

    @Value("${afip.alicuota.iva:21.0}")
    private double alicuotaDefault;

    @Value("${afip.pto.vta:1}")
    private int ptoVtaDefault;

    @Autowired
    private FacturaVentaRepository facturaVentaRepository;

    @Autowired
    private PuntoVentaRepository puntoVentaRepository;

    @Autowired
    private CondicionIvaRepository condicionIvaRepository;

    /**
     * Orquesta la obtención del Token WSAA, consulta el último comprobante y solicita el CAE.
     * Si el request trae el id de una factura existente, guarda el CAE en esa factura.
     */
    @Transactional
    public FECAEResponse emitirFacturaAfip(FacturaRequestDTO req) throws Exception {
        if (req.getId() != null) {
            FacturaVenta existente = facturaVentaRepository.findById(req.getId()).orElse(null);
            if (existente != null && existente.getCae() != null && !existente.getCae().isBlank()) {
                throw new FacturaConCaeException("La factura " + existente.getNumero() + " ya tiene CAE asignado: " + existente.getCae());
            }
        }
        completarDatosAfip(req);

        // 1. Obtener Token y Sign (el ticket queda guardado hasta que venza)
        String[] auth;
        if (keystorePath != null && !keystorePath.isBlank()) {
            auth = AfipWsaaClient.obtenerTokenYSignP12(keystorePath, keystorePassword);
        } else {
            auth = AfipWsaaClient.obtenerTokenYSign(
                    keyPath,
                    certPath,
                    servicio,
                    isProduccion,
                    String.valueOf(cuitEmisor)
            );
        }
        String token = auth[0];
        String sign = auth[1];

        // 2. Consultar el último comprobante autorizado en AFIP
        int ultimoNro = AfipWsfeClient.obtenerUltimoComprobante(
                token, sign, cuitEmisor, req.getPtoVta(), req.getCbteTipo(), isProduccion
        );
        int nuevoNro = ultimoNro + 1;

        // 3. Solicitar el CAE utilizando el servicio de facturación
        FECAEResponse response = AfipFacturaService.emitirComprobante(token, sign, cuitEmisor, nuevoNro, req);

        if (req.getId() != null) {
            guardarCae(req.getId(), response);
        }
        return response;
    }

    private void completarDatosAfip(FacturaRequestDTO req) {
        if (isVacio(req.getPtoVta()) && req.getPuntoVentaId() != null) {
            puntoVentaRepository.findById(req.getPuntoVentaId())
                    .ifPresent(pv -> req.setPtoVta(pv.getNumero()));
        }
        if (isVacio(req.getCondicionIvaReceptor()) && req.getCondicionIvaId() != null) {
            condicionIvaRepository.findById(req.getCondicionIvaId())
                    .ifPresent(ci -> req.setCondicionIvaReceptor(ci.getCodigoAfip()));
        }
        if (isVacio(req.getPtoVta())) req.setPtoVta(ptoVtaDefault);
        if (isVacio(req.getCbteTipo())) req.setCbteTipo(cbteTipoDefault);
        if (isVacio(req.getConcepto())) req.setConcepto(1);
        if (isVacio(req.getDocTipo())) req.setDocTipo(99);
        if (req.getDocNro() == null) req.setDocNro(0L);
        if (isVacio(req.getCondicionIvaReceptor())) req.setCondicionIvaReceptor(5);
        if (req.getAlicuotaIvaPct() == null || req.getAlicuotaIvaPct() == 0) req.setAlicuotaIvaPct(alicuotaDefault);
        if (req.getImporteTotal() == null && req.getId() != null) {
            facturaVentaRepository.findById(req.getId())
                    .ifPresent(f -> req.setImporteTotal(f.getImporteTotal()));
        }
    }

    private static boolean isVacio(Integer valor) {
        return valor == null || valor == 0;
    }

    private void guardarCae(Long facturaId, FECAEResponse response) throws Exception {
        FacturaVenta factura = facturaVentaRepository.findById(facturaId).orElse(null);
        if (factura == null || response.getFeDetResp() == null
                || response.getFeDetResp().getFECAEDetResponse().isEmpty()) {
            return;
        }
        FECAEDetResponse det = response.getFeDetResp().getFECAEDetResponse().get(0);
        factura.setResultadoAfip(det.getResultado());
        if ("A".equals(det.getResultado())) {
            factura.setCae(det.getCAE());
            factura.setNroComprobanteAfip((int) det.getCbteDesde());
            factura.setTipoComprobanteAfip(response.getFeCabResp().getCbteTipo());
            factura.setCaeFechaVencimiento(new SimpleDateFormat("yyyyMMdd").parse(det.getCAEFchVto()));
            factura.setMotivoRechazo(null);
        } else {
            List<String> motivos = new ArrayList<>();
            if (det.getObservaciones() != null) {
                for (Obs o : det.getObservaciones().getObs()) {
                    motivos.add("[" + o.getCode() + "] " + o.getMsg());
                }
            }
            factura.setMotivoRechazo(String.join(" | ", motivos));
        }
        factura.setFechaModificacion(new Date());
        facturaVentaRepository.save(factura);
    }
}
