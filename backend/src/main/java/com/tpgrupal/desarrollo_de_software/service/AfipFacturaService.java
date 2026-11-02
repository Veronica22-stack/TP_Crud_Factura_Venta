package com.tpgrupal.desarrollo_de_software.service;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.xml.ws.BindingProvider;

import com.tpgrupal.desarrollo_de_software.dto.FacturaRequestDTO;
import com.tpgrupal.desarrollo_de_software.util.AfipHttpUtil;

import ar.gov.afip.wsfe.AlicIva;
import ar.gov.afip.wsfe.ArrayOfAlicIva;
import ar.gov.afip.wsfe.ArrayOfFECAEDetRequest;
import ar.gov.afip.wsfe.Err;
import ar.gov.afip.wsfe.FEAuthRequest;
import ar.gov.afip.wsfe.FECAECabRequest;
import ar.gov.afip.wsfe.FECAEDetRequest;
import ar.gov.afip.wsfe.FECAEDetResponse;
import ar.gov.afip.wsfe.FECAERequest;
import ar.gov.afip.wsfe.FECAEResponse;
import ar.gov.afip.wsfe.Obs;
import ar.gov.afip.wsfe.Service;
import ar.gov.afip.wsfe.ServiceSoap;

public class AfipFacturaService {

    private static final int TIMEOUT_MS = 60000;

    public static FECAEResponse emitirComprobante(String token, String sign, long cuitEmisor, int cbteNro, FacturaRequestDTO req) {
        URL wsdl = AfipFacturaService.class.getResource("/wsdl/WSFEv1.wsdl");
        Service service = new Service(wsdl);
        ServiceSoap port = service.getServiceSoap();

        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            AfipHttpUtil.getURLWsfe(false)
        );
        bp.getRequestContext().put("com.sun.xml.ws.connect.timeout", TIMEOUT_MS);
        bp.getRequestContext().put("com.sun.xml.ws.request.timeout", TIMEOUT_MS);

        // 1. Autenticacion
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(token.trim());
        auth.setSign(sign.trim());
        auth.setCuit(cuitEmisor);

        System.out.println("Enviando factura Pto de Venta..." + req.getPtoVta());
        System.out.println("Enviando factura Tipo Cte..." + req.getCbteTipo());
        // 2. Cabecera
        FECAECabRequest cab = new FECAECabRequest();
        cab.setCantReg(1);
        cab.setPtoVta(req.getPtoVta());
        cab.setCbteTipo(req.getCbteTipo());

        // 3. Detalle generico
        FECAEDetRequest det = new FECAEDetRequest();
        det.setConcepto(req.getConcepto());
        det.setDocTipo(req.getDocTipo());
        det.setDocNro(req.getDocNro());
        det.setCbteDesde(cbteNro);
        det.setCbteHasta(cbteNro);

        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        det.setCbteFch(fechaHoy);

        // Fechas requeridas si se prestan Servicios (Concepto 2 o 3)
        if (req.getConcepto() == 2 || req.getConcepto() == 3) {
            det.setFchServDesde(req.getFchServDesde() != null ? req.getFchServDesde() : fechaHoy);
            det.setFchServHasta(req.getFchServHasta() != null ? req.getFchServHasta() : fechaHoy);
            det.setFchVtoPago(req.getFchVtoPago() != null ? req.getFchVtoPago() : fechaHoy);
        }

        det.setMonId("PES");
        det.setMonCotiz(1.0);
        det.setCondicionIVAReceptorId(req.getCondicionIvaReceptor());

        // 4. Desglose de IVA segun el tipo de comprobante
        if (req.getImporteTotal() == null) {
            throw new IllegalArgumentException("Falta el importe total de la factura.");
        }
        double impTotal = req.getImporteTotal();

        if (req.getCbteTipo() == 11) {
            // Factura C (Monotributo / Exento): sin IVA desglosado
            det.setImpTotal(impTotal);
            det.setImpTotConc(0.00);
            det.setImpNeto(impTotal);
            det.setImpOpEx(0.00);
            det.setImpTrib(0.00);
            det.setImpIVA(0.00);
        } else {
            // Factura A o B (Responsable Inscripto): requiere calculo y nodo <Iva>
            double alicuota = req.getAlicuotaIvaPct();
            double impNeto = Math.round((impTotal / (1.0 + (alicuota / 100.0))) * 100.0) / 100.0;
            double impIva = Math.round((impTotal - impNeto) * 100.0) / 100.0;

            det.setImpTotal(impTotal);
            det.setImpTotConc(0.00);
            det.setImpNeto(impNeto);
            det.setImpOpEx(0.00);
            det.setImpTrib(0.00);
            det.setImpIVA(impIva);

            // ID de alicuota AFIP (5 = 21%, 4 = 10.5%, 6 = 27%)
            int idAlicuotaAfip = obtenerIdAlicuotaAfip(alicuota);

            AlicIva alicIva = new AlicIva();
            alicIva.setId(idAlicuotaAfip);
            alicIva.setBaseImp(impNeto);
            alicIva.setImporte(impIva);

            ArrayOfAlicIva arrayIva = new ArrayOfAlicIva();
            arrayIva.getAlicIva().add(alicIva);
            det.setIva(arrayIva);
        }

        ArrayOfFECAEDetRequest detArray = new ArrayOfFECAEDetRequest();
        detArray.getFECAEDetRequest().add(det);

        FECAERequest feCAEReq = new FECAERequest();
        feCAEReq.setFeCabReq(cab);
        feCAEReq.setFeDetReq(detArray);

        FECAEResponse response = port.fecaeSolicitar(auth, feCAEReq);
        if (response == null) {
            throw new RuntimeException("La respuesta de AFIP/ARCA fue nula.");
        }

        // Errores a nivel general (token, CUIT, formato)
        if (response.getErrors() != null && response.getErrors().getErr() != null
                && !response.getErrors().getErr().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Err err : response.getErrors().getErr()) {
                System.err.println("Error AFIP " + err.getCode() + ": " + err.getMsg());
                sb.append("[").append(err.getCode()).append("] ").append(err.getMsg()).append(" ");
            }
            throw new RuntimeException("AFIP devolvio errores: " + sb.toString().trim());
        }

        if (response.getFeDetResp() == null || response.getFeDetResp().getFECAEDetResponse() == null
                || response.getFeDetResp().getFECAEDetResponse().isEmpty()) {
            throw new RuntimeException("AFIP no devolvio detalles de comprobantes en la respuesta.");
        }

        FECAEDetResponse detResp = response.getFeDetResp().getFECAEDetResponse().get(0);

        // A = Aprobado, R = Rechazado, P = Parcial
        String resultado = detResp.getResultado();
        System.out.println("Resultado de la operacion: " + resultado);

        if ("A".equals(resultado)) {
            System.out.println("Comprobante autorizado");
            System.out.println("CAE: " + detResp.getCAE());
            System.out.println("Vencimiento CAE: " + detResp.getCAEFchVto());
        } else {
            System.err.println("Comprobante RECHAZADO.");
            if (detResp.getObservaciones() != null && detResp.getObservaciones().getObs() != null) {
                for (Obs obs : detResp.getObservaciones().getObs()) {
                    System.err.println("Observacion " + obs.getCode() + ": " + obs.getMsg());
                }
            }
        }

        return response;
    }

    private static int obtenerIdAlicuotaAfip(double alicuotaPct) {
        if (alicuotaPct == 21.0) return 5;
        if (alicuotaPct == 10.5) return 4;
        if (alicuotaPct == 27.0) return 6;
        if (alicuotaPct == 5.0)  return 8;
        if (alicuotaPct == 2.5)  return 9;
        return 5; // por defecto 21%
    }
}
