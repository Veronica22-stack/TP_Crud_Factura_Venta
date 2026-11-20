package com.tpgrupal.desarrollo_de_software;

import com.tpgrupal.desarrollo_de_software.controller.FacturaRestController;
import com.tpgrupal.desarrollo_de_software.dto.*;
import com.tpgrupal.desarrollo_de_software.exception.ResourceNotFoundException;

import com.tpgrupal.desarrollo_de_software.service.BusquedaCatalogoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del ABM de facturas contra la base configurada en application.properties.
 * Cada ítem usa un producto real del catálogo (el backend exige un producto elegido y toma
 * el precio de la lista), y las facturas que crea cada prueba se borran al terminar.
 */
@SpringBootTest
public class FacturaCrudIntegrationTest {

    private static final String FIDEOS = "ART-201";
    private static final String ARROZ = "ART-202";

    @Autowired
    private FacturaRestController facturaRestController;

    @Autowired
    private BusquedaCatalogoService busquedaCatalogoService;

    private final List<Long> facturasCreadas = new ArrayList<>();

    @AfterEach
    void borrarFacturasDePrueba() {
        for (Long id : facturasCreadas) {
            try {
                facturaRestController.eliminarFactura(id);
            } catch (Exception ignorada) {
                // ya la borró la prueba
            }
        }
        facturasCreadas.clear();
    }

    // Producto del catálogo por código (con el mismo buscador paginado de la pantalla),
    // en la primera lista de precios que lo tenga
    private ListaPrecioArticuloDTO producto(String codigo) {
        return busquedaCatalogoService.buscarProductos(null, null, null, codigo, null, 0, 100).getContent().stream()
                .filter(p -> codigo.equals(p.getCodigo()))
                .min(Comparator.comparing(ListaPrecioArticuloDTO::getListaPrecioId))
                .orElseThrow(() -> new IllegalStateException(
                        "Falta el producto " + codigo + " en la base: cargar datos_prueba_facturacion.sql"));
    }

    private FacturaDetalleRequestDTO item(ListaPrecioArticuloDTO producto, double cantidad) {
        return FacturaDetalleRequestDTO.builder()
                .listaPrecioArticuloId(producto.getId())
                .articuloId(producto.getArticuloId())
                .productoDescripcion(producto.getDenominacion())
                .cantidad(cantidad)
                .precioUnitario(producto.getPrecioVenta())
                .build();
    }

    // Factura sin cliente: primer punto de venta y condición de IVA Consumidor Final (como el formulario)
    private FacturaRequestDTO.FacturaRequestDTOBuilder facturaConsumidorFinal(FacturaDetalleRequestDTO... items) {
        OpcionesComboDTO opciones = facturaRestController.obtenerOpciones().getBody();
        assertNotNull(opciones);
        Long puntoVentaId = opciones.getPuntosVenta().get(0).getId();
        Long consumidorFinalId = opciones.getCondicionesIva().stream()
                .filter(ci -> ci.getDescripcion() != null && ci.getDescripcion().toLowerCase().contains("consumidor"))
                .map(OpcionesComboDTO.ItemComboDTO::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta la condición de IVA Consumidor Final en la base"));
        return FacturaRequestDTO.builder()
                .fechaEmision(new Date())
                .puntoVentaId(puntoVentaId)
                .condicionIvaId(consumidorFinalId)
                .clienteDenominacion("Consumidor Final")
                .detalles(List.of(items));
    }

    private ResponseEntity<FacturaReporteDTO> crear(FacturaRequestDTO request) {
        ResponseEntity<FacturaReporteDTO> response = facturaRestController.crearFactura(request);
        assertNotNull(response.getBody());
        facturasCreadas.add(response.getBody().getId());
        return response;
    }

    @Test
    @DisplayName("Crear factura")
    void testCrearFactura() {
        ListaPrecioArticuloDTO fideos = producto(FIDEOS);
        ListaPrecioArticuloDTO arroz = producto(ARROZ);
        FacturaRequestDTO request = facturaConsumidorFinal(item(fideos, 1), item(arroz, 1))
                .observaciones("Factura de prueba de creación")
                .build();

        ResponseEntity<FacturaReporteDTO> response = crear(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        assertEquals(fideos.getPrecioVenta() + arroz.getPrecioVenta(), response.getBody().getImporteTotal(), 0.001);
        assertEquals(2, response.getBody().getCantidadItems());
        assertEquals("EMITIDA", response.getBody().getEstado());
    }

    @Test
    @DisplayName("Crear factura sin producto elegido es rechazada")
    void testCrearFacturaSinProducto() {
        FacturaRequestDTO request = facturaConsumidorFinal(FacturaDetalleRequestDTO.builder()
                .productoDescripcion("Producto libre")
                .cantidad(1.0)
                .precioUnitario(100.0)
                .build()).build();

        assertThrows(IllegalArgumentException.class, () -> facturaRestController.crearFactura(request));
    }

    @Test
    @DisplayName("Actualizar factura")
    void testActualizarFactura() {
        Long id = crear(facturaConsumidorFinal(item(producto(FIDEOS), 2)).build()).getBody().getId();

        ListaPrecioArticuloDTO arroz = producto(ARROZ);
        FacturaRequestDTO reqUpdate = FacturaRequestDTO.builder()
                .observaciones("Actualizado exitosamente")
                .detalles(List.of(item(arroz, 3)))
                .build();

        ResponseEntity<FacturaReporteDTO> actualizadaRes = facturaRestController.actualizarFactura(id, reqUpdate);

        assertEquals(HttpStatus.OK, actualizadaRes.getStatusCode());
        assertEquals(3 * arroz.getPrecioVenta(), actualizadaRes.getBody().getImporteTotal(), 0.001);
        assertEquals("Actualizado exitosamente", actualizadaRes.getBody().getObservaciones());
    }

    @Test
    @DisplayName("Anular factura")
    void testAnularFactura() {
        Long id = crear(facturaConsumidorFinal(item(producto(FIDEOS), 1)).build()).getBody().getId();

        ResponseEntity<FacturaReporteDTO> anuladaRes = facturaRestController.actualizarEstado(id, "ANULADA", "Anulación por prueba");

        assertEquals(HttpStatus.OK, anuladaRes.getStatusCode());
        assertEquals("ANULADA", anuladaRes.getBody().getEstado());
    }

    @Test
    @DisplayName("Eliminar factura")
    void testEliminarFactura() {
        Long id = crear(facturaConsumidorFinal(item(producto(FIDEOS), 1)).build()).getBody().getId();

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
    }
}
