package com.tpgrupal.desarrollo_de_software.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, java.util.NoSuchElementException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex instanceof IllegalArgumentException && ex.getMessage() != null
                ? ex.getMessage()
                : "El cuerpo de la petición (JSON) es requerido o contiene datos inválidos.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Número de factura repetido (validación del servicio o restricción UNIQUE de la base) -> 409 Conflict
    @ExceptionHandler({DuplicateResourceException.class, org.springframework.dao.DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex instanceof DuplicateResourceException
                ? ex.getMessage()
                : explicarErrorBase(ex));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // La base rechazó el dato: se informa la causa real (no siempre es un número repetido)
    private static String explicarErrorBase(Exception ex) {
        Throwable causa = ex;
        while (causa.getCause() != null && causa.getCause() != causa) causa = causa.getCause();
        String detalle = causa.getMessage() != null ? causa.getMessage() : ex.getMessage();
        String m = detalle == null ? "" : detalle.toLowerCase();
        System.err.println("Error de integridad de la base: " + detalle);
        if (m.contains("duplicate entry") && m.contains("numero")) {
            return "Ya existe una factura con ese número. Vuelva a intentar.";
        }
        if (m.contains("cannot be null")) {
            java.util.regex.Matcher col = java.util.regex.Pattern.compile("column '([^']+)'").matcher(detalle);
            String columna = col.find() ? col.group(1) : "desconocido";
            return "Falta un dato obligatorio para guardar (" + columna + ").";
        }
        if (m.contains("foreign key")) {
            return "Uno de los datos elegidos no existe en la base (cliente, punto de venta, moneda o producto).";
        }
        return "La base de datos rechazó la operación: " + detalle;
    }

    @ExceptionHandler(FacturaConCaeException.class)
    public ResponseEntity<Map<String, Object>> handleFacturaConCae(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Operación no permitida por el estado de la factura (anulada, fecha fuera del rango de ARCA, etc.)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoInvalido(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Configuración o comunicación con ARCA (certificado, red, timeout) -> 502 Bad Gateway
    @ExceptionHandler(AfipException.class)
    public ResponseEntity<Map<String, Object>> handleAfip(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_GATEWAY.value());
        body.put("error", "Bad Gateway");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
