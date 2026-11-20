package com.tpgrupal.desarrollo_de_software.controller;

import com.tpgrupal.desarrollo_de_software.dto.*;
import com.tpgrupal.desarrollo_de_software.service.FacturaService;
import com.tpgrupal.desarrollo_de_software.service.ReporteService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
public class FacturaRestController {

    private final FacturaService facturaService;
    private final ReporteService reporteService;

    public FacturaRestController(FacturaService facturaService, ReporteService reporteService) {
        this.facturaService = facturaService;
        this.reporteService = reporteService;
    }

    // Listar facturas con filtros y paginación
    @GetMapping
    public ResponseEntity<PageResponse<FacturaReporteDTO>> listarFacturas(
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(defaultValue = "numero_desc") String orden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        PageResponse<FacturaReporteDTO> resultado = facturaService.buscarFacturasPaginadas(
                numero, cliente, clienteId, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId, orden, page, size);

        return ResponseEntity.ok(resultado);
    }

    // Totales del panel: total facturado, con CAE, pendientes, anuladas y últimas facturas
    @GetMapping("/resumen")
    public ResponseEntity<ResumenFacturacionDTO> obtenerResumen(@RequestParam(defaultValue = "5") int ultimas) {
        return ResponseEntity.ok(facturaService.obtenerResumen(ultimas));
    }

    // Obtener factura por ID
    @GetMapping("/{id}")
    public ResponseEntity<FacturaReporteDTO> obtenerPorId(@PathVariable Long id) {
        FacturaReporteDTO factura = facturaService.obtenerFacturaPorId(id);
        return ResponseEntity.ok(factura);
    }

    // Obtener formato formulario por número
    @GetMapping("/form/{numero}")
    public ResponseEntity<FacturaRequestDTO> obtenerPorNumero(@PathVariable Long numero) {
        FacturaRequestDTO factura = facturaService.obtenerFacturaPorNumero(numero);
        return ResponseEntity.ok(factura);
    }

    // Crear nueva factura
    @PostMapping
    public ResponseEntity<FacturaReporteDTO> crearFactura(@RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO facturaCreada = facturaService.crearFactura(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaCreada);
    }
    // Pide el CAE a ARCA (real o simulado según afip.modo). Body opcional: {"tipoComprobante": 11}
    // Si ARCA rechaza, responde 200 igual con resultadoAfip = "R" y motivoRechazo.
    @PostMapping("/{id}/emitir")
    public ResponseEntity<FacturaReporteDTO> emitirCae(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        Integer tipo = null;
        Object valor = body == null ? null : body.get("tipoComprobante");
        if (valor instanceof Number n) tipo = n.intValue();
        else if (valor != null && !valor.toString().isBlank()) tipo = Integer.valueOf(valor.toString().trim());
        return ResponseEntity.ok(facturaService.emitirCae(id, tipo));
    }

    // Ruta anterior (body con el id de la factura): se mantiene por compatibilidad
    @PostMapping("/emitir-afip")
    public ResponseEntity<FacturaReporteDTO> emitirFacturaAfip(@RequestBody FacturaRequestDTO request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Falta el id de la factura.");
        }
        return ResponseEntity.ok(facturaService.emitirCae(request.getId(), request.getCbteTipo()));
    }
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> imprimirFacturaPdf(@PathVariable Long id) {
        byte[] pdf = facturaService.generarPdfFactura(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factura-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Actualizar factura existente
    @PutMapping("/{id}")
    public ResponseEntity<FacturaReporteDTO> actualizarFactura(
            @PathVariable Long id,
            @RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO facturaActualizada = facturaService.actualizarFactura(id, request);
        return ResponseEntity.ok(facturaActualizada);
    }

    // Actualizar estado de factura
    @PutMapping("/{id}/estado")
    public ResponseEntity<FacturaReporteDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String observaciones) {
        FacturaReporteDTO facturaActualizada = facturaService.actualizarEstado(id, estado, observaciones);
        return ResponseEntity.ok(facturaActualizada);
    }

    // Anular factura
    @PutMapping("/{id}/anular")
    public ResponseEntity<Void> anularFactura(@PathVariable Long id) {
        facturaService.anularFactura(id);
        return ResponseEntity.ok().build();
    }

    // Eliminar factura
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }

    // Opciones para combos del formulario
    @GetMapping("/opciones")
    public ResponseEntity<OpcionesComboDTO> obtenerOpciones() {
        return ResponseEntity.ok(facturaService.obtenerOpcionesCombo());
    }

    // Exportar a PDF con filtros
    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportarPdf(
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(defaultValue = "numero_desc") String orden) {

        List<FacturaReporteDTO> facturas = facturaService.buscarFacturasFiltradas(
                numero, cliente, clienteId, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId, orden);
        ByteArrayInputStream bis = reporteService.generarPdf(facturas);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=reporte-facturas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // Exportar a Excel (TSV) con filtros
    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(defaultValue = "numero_desc") String orden) {

        List<FacturaReporteDTO> facturas = facturaService.buscarFacturasFiltradas(
                numero, cliente, clienteId, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId, orden);
        ByteArrayInputStream bis = reporteService.generarExcelTsv(facturas);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte-facturas.xls");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(bis));
    }

    // Exportar a texto separado por tabulaciones (.txt) con filtros
    @GetMapping("/export/txt")
    public ResponseEntity<InputStreamResource> exportarTxt(
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(defaultValue = "numero_desc") String orden) {

        List<FacturaReporteDTO> facturas = facturaService.buscarFacturasFiltradas(
                numero, cliente, clienteId, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId, orden);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-facturas.txt")
                .contentType(MediaType.parseMediaType("text/tab-separated-values; charset=UTF-8"))
                .body(new InputStreamResource(reporteService.generarTxt(facturas)));
    }
}
