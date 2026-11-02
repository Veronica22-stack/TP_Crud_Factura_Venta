package com.tpgrupal.desarrollo_de_software;

import com.tpgrupal.desarrollo_de_software.controller.FacturaRestController;
import com.tpgrupal.desarrollo_de_software.dto.*;
import com.tpgrupal.desarrollo_de_software.exception.ResourceNotFoundException;
import com.tpgrupal.desarrollo_de_software.service.FacturaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FacturaCrudIntegrationTest {

    @Autowired
    private FacturaRestController facturaRestController;

    @Autowired
    private FacturaService facturaService;

    @Test
    @DisplayName("Crear factura")
    void testCrearFactura() {
        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .fechaEmision(new Date())
                .clienteDenominacion("Consumidor Final")
                .condicionIva("IVA Responsable Inscripto")
                .puntoVentaDescripcion("Punto de Venta 6")
                .observaciones("Factura de prueba de creación")
                .detalles(List.of(
                        FacturaDetalleRequestDTO.builder()
                                .productoDescripcion("FIDEOS")
                                .cantidad(1.0)
                                .precioUnitario(500.0)
                                .build(),
                        FacturaDetalleRequestDTO.builder()
                                .productoDescripcion("ARROZ")
                                .cantidad(1.0)
                                .precioUnitario(1500.0)
                                .build()
                ))
                .build();

        ResponseEntity<FacturaReporteDTO> response = facturaRestController.crearFactura(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(2000.0, response.getBody().getImporteTotal());
        assertEquals(2, response.getBody().getCantidadItems());
        assertEquals("EMITIDA", response.getBody().getEstado());
    }

    @Test
    @DisplayName("Actualizar factura")
    void testActualizarFactura() {
        FacturaRequestDTO reqCrear = FacturaRequestDTO.builder()
                .fechaEmision(new Date())
                .clienteDenominacion("Consumidor Final")
                .detalles(List.of(
                        FacturaDetalleRequestDTO.builder()
                                .productoDescripcion("FIDEOS")
                                .cantidad(2.0)
                                .precioUnitario(500.0)
                                .build()
                ))
                .build();

        ResponseEntity<FacturaReporteDTO> creadaRes = facturaRestController.crearFactura(reqCrear);
        Long id = creadaRes.getBody().getId();

        FacturaRequestDTO reqUpdate = FacturaRequestDTO.builder()
                .observaciones("Actualizado exitosamente")
                .detalles(List.of(
                        FacturaDetalleRequestDTO.builder()
                                .productoDescripcion("ARROZ")
                                .cantidad(3.0)
                                .precioUnitario(1500.0)
                                .build()
                ))
                .build();

        ResponseEntity<FacturaReporteDTO> actualizadaRes = facturaRestController.actualizarFactura(id, reqUpdate);

        assertEquals(HttpStatus.OK, actualizadaRes.getStatusCode());
        assertEquals(4500.0, actualizadaRes.getBody().getImporteTotal());
        assertEquals("Actualizado exitosamente", actualizadaRes.getBody().getObservaciones());
    }

    @Test
    @DisplayName("Anular factura")
    void testAnularFactura() {
        FacturaRequestDTO reqCrear = FacturaRequestDTO.builder()
                .fechaEmision(new Date())
                .clienteDenominacion("Consumidor Final")
                .detalles(List.of(
                        FacturaDetalleRequestDTO.builder()
                                .productoDescripcion("FIDEOS")
                                .cantidad(1.0)
                                .precioUnitario(500.0)
                                .build()
                ))
                .build();

        ResponseEntity<FacturaReporteDTO> creadaRes = facturaRestController.crearFactura(reqCrear);
        Long id = creadaRes.getBody().getId();

        ResponseEntity<FacturaReporteDTO> anuladaRes = facturaRestController.actualizarEstado(id, "ANULADA", "Anulación por prueba");

        assertEquals(HttpStatus.OK, anuladaRes.getStatusCode());
        assertEquals("ANULADA", anuladaRes.getBody().getEstado());
    }

    @Test
    @DisplayName("Eliminar factura")
    void testEliminarFactura() {
        FacturaRequestDTO reqCrear = FacturaRequestDTO.builder()
                .fechaEmision(new Date())
                .clienteDenominacion("Consumidor Final")
                .detalles(List.of(
                        FacturaDetalleRequestDTO.builder()
                                .productoDescripcion("FIDEOS")
                                .cantidad(1.0)
                                .precioUnitario(500.0)
                                .build()
                ))
                .build();

        ResponseEntity<FacturaReporteDTO> creadaRes = facturaRestController.crearFactura(reqCrear);
        Long id = creadaRes.getBody().getId();

        ResponseEntity<Void> deleteRes = facturaRestController.eliminarFactura(id);
        assertEquals(HttpStatus.NO_CONTENT, deleteRes.getStatusCode());

        assertThrows(ResourceNotFoundException.class, () -> facturaRestController.obtenerPorId(id));
    }

    @Test
    @DisplayName("Obtener opciones para combos")
    void testObtenerOpcionesCombos() {
        ResponseEntity<OpcionesComboDTO> response = facturaRestController.obtenerOpciones();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getPuntosVenta().isEmpty());
        assertFalse(response.getBody().getCondicionesIva().isEmpty());
        assertFalse(response.getBody().getClientes().isEmpty());
        assertFalse(response.getBody().getArticulos().isEmpty());
    }
}
