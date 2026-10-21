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
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        PageResponse<FacturaReporteDTO> resultado = facturaService.buscarFacturasPaginadas(
                numero, cliente, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId, page, size);

        return ResponseEntity.ok(resultado);
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
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId) {

        List<FacturaReporteDTO> facturas = facturaService.buscarFacturasFiltradas(
                numero, cliente, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId);
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
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo,
            @RequestParam(required = false) Long listaPrecioId) {

        List<FacturaReporteDTO> facturas = facturaService.buscarFacturasFiltradas(
                numero, cliente, condicionIva, fechaDesde, fechaHasta, estado, montoMinimo, listaPrecioId);
        ByteArrayInputStream bis = reporteService.generarExcelTsv(facturas);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte-facturas.xls");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(bis));
    }
}
