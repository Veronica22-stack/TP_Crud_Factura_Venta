package com.tpgrupal.desarrollo_de_software.exception;

/**
 * Error de configuración o de comunicación con ARCA (WSAA / WSFEv1): certificado que falta,
 * servidor caído, timeout, respuesta inválida. Se informa como 502 Bad Gateway.
 * Los rechazos de negocio (comprobante con datos inválidos) NO usan esta excepción:
 * quedan guardados en la factura (resultadoAfip = R y motivoRechazo).
 */
public class AfipException extends RuntimeException {
    public AfipException(String message) {
        super(message);
    }

    public AfipException(String message, Throwable cause) {
        super(message, cause);
    }
}
