package com.tpgrupal.desarrollo_de_software;

import com.tpgrupal.desarrollo_de_software.dto.ClienteDTO;
import com.tpgrupal.desarrollo_de_software.dto.FacturaDetalleReporteDTO;
import com.tpgrupal.desarrollo_de_software.dto.FacturaReporteDTO;
import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioArticuloDTO;
import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioDTO;
import com.tpgrupal.desarrollo_de_software.dto.PageResponse;
import com.tpgrupal.desarrollo_de_software.service.BusquedaCatalogoService;
import com.tpgrupal.desarrollo_de_software.service.FacturaService;
import com.tpgrupal.desarrollo_de_software.service.ListaPrecioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exploradores de productos y clientes paginados en el backend (LIMIT / OFFSET).
 * Solo lee la base configurada (datos de datos_prueba_facturacion.sql).
 */
@SpringBootTest
class BusquedaCatalogoPaginadaTest {

    @Autowired
    private BusquedaCatalogoService busqueda;

    @Autowired
    private ListaPrecioService listaPrecioService;

    @Autowired
    private FacturaService facturaService;

    private Long listaId;

    @BeforeEach
    void primeraLista() {
        listaId = listaPrecioService.listarListasPrecio().stream()
                .map(ListaPrecioDTO::getId).min(Long::compare).orElseThrow();
    }

    // --- Productos ---

    @Test
    void laPaginaTraeComoMaximoElTamanioPedidoYElTotalDeLaLista() {
        PageResponse<ListaPrecioArticuloDTO> pagina = busqueda.buscarProductos(null, listaId, null, null, null, 0, 5);

        assertEquals(5, pagina.getContent().size());
        assertTrue(pagina.getTotalElements() > 5);
        assertEquals((int) Math.ceil(pagina.getTotalElements() / 5.0), pagina.getTotalPages());
        assertTrue(pagina.getContent().stream().allMatch(p -> listaId.equals(p.getListaPrecioId())));
    }

    @Test
    void paginasSeguidasNoRepitenProductos() {
        List<Long> primera = ids(busqueda.buscarProductos(null, listaId, null, null, null, 0, 4));
        List<Long> segunda = ids(busqueda.buscarProductos(null, listaId, null, null, null, 1, 4));
        List<Long> ambas = ids(busqueda.buscarProductos(null, listaId, null, null, null, 0, 8));

        Set<Long> union = new HashSet<>(primera);
        union.addAll(segunda);
        assertEquals(8, union.size());
        assertEquals(ambas, concat(primera, segunda));
    }

    @Test
    void filtraPorRubroExacto() {
        PageResponse<ListaPrecioArticuloDTO> alimentos = busqueda.buscarProductos(null, listaId, "Alimentos", null, null, 0, 100);
        PageResponse<ListaPrecioArticuloDTO> bebidas = busqueda.buscarProductos(null, listaId, "Bebidas", null, null, 0, 100);

        assertFalse(alimentos.getContent().isEmpty());
        assertTrue(alimentos.getContent().stream().allMatch(p -> "Alimentos".equals(p.getRubro())));
        assertTrue(bebidas.getContent().stream().allMatch(p -> "Bebidas".equals(p.getRubro())));
        assertNotEquals(alimentos.getTotalElements(), 0);
    }

    @Test
    void filtraPorCodigoYPorDenominacionSinImportarTildes() {
        PageResponse<ListaPrecioArticuloDTO> porCodigo = busqueda.buscarProductos(null, listaId, null, "art-20", null, 0, 100);
        PageResponse<ListaPrecioArticuloDTO> porNombre = busqueda.buscarProductos(null, listaId, null, null, "ergonomica", 0, 100);

        assertFalse(porCodigo.getContent().isEmpty());
        assertTrue(porCodigo.getContent().stream().allMatch(p -> p.getCodigo().toLowerCase().contains("art-20")));
        assertEquals(List.of("ART-102"), porNombre.getContent().stream().map(ListaPrecioArticuloDTO::getCodigo).toList());
    }

    @Test
    void losFiltrosSeCombinan() {
        PageResponse<ListaPrecioArticuloDTO> page = busqueda.buscarProductos(null, listaId, "Alimentos", null, "fideos", 0, 100);

        assertFalse(page.getContent().isEmpty());
        assertTrue(page.getContent().stream()
                .allMatch(p -> "Alimentos".equals(p.getRubro()) && p.getDenominacion().toLowerCase().contains("fideos")));
    }

    @Test
    void rubrosDeUnaListaSinRepetirYOrdenados() {
        List<String> rubros = busqueda.rubros(listaId);

        assertTrue(rubros.containsAll(List.of("Alimentos", "Bebidas", "Muebles")));
        assertEquals(rubros.stream().distinct().sorted().toList(), rubros);
    }

    // --- Clientes ---

    @Test
    void clientesPaginadosPorTextoEnNombreOCuit() {
        ClienteDTO alguno = busqueda.buscarClientes(null, null, null, 0, 1).getContent().get(0);
        String parteNombre = alguno.getDenominacion().substring(0, 3);

        PageResponse<ClienteDTO> porNombre = busqueda.buscarClientes(parteNombre, null, null, 0, 100);
        assertTrue(porNombre.getContent().stream().anyMatch(c -> c.getId().equals(alguno.getId())));

        PageResponse<ClienteDTO> conCuit = busqueda.buscarClientes(null, null, null, 0, 100);
        ClienteDTO conDigitos = conCuit.getContent().stream()
                .filter(c -> c.getCuitCuil() != null && c.getCuitCuil().replaceAll("\\D", "").length() >= 6)
                .findFirst().orElseThrow();
        String digitos = conDigitos.getCuitCuil().replaceAll("\\D", "").substring(2, 8);
        PageResponse<ClienteDTO> porCuit = busqueda.buscarClientes(digitos, null, null, 0, 100);
        assertTrue(porCuit.getContent().stream().anyMatch(c -> c.getId().equals(conDigitos.getId())));
    }

    @Test
    void clientesRespetanElTamanioDePagina() {
        PageResponse<ClienteDTO> pagina = busqueda.buscarClientes(null, null, null, 0, 2);

        assertEquals(2, pagina.getContent().size());
        assertTrue(pagina.getTotalElements() >= 2);
    }

    // --- Detalle de factura con rubro (la edición ya no necesita el catálogo entero) ---

    @Test
    void elDetalleDeLaFacturaTraeCodigoYRubro() {
        FacturaReporteDTO factura = facturaService.buscarFacturasPaginadas(
                null, null, null, null, null, null, null, null, null, "numero_asc", 0, 1).getContent().get(0);

        List<FacturaDetalleReporteDTO> detalles = facturaService.obtenerFacturaPorId(factura.getId()).getDetalles();

        assertFalse(detalles.isEmpty());
        assertTrue(detalles.stream().allMatch(d -> d.getCodigo() != null && d.getRubro() != null && !d.getRubro().isBlank()));
    }

    private static List<Long> ids(PageResponse<ListaPrecioArticuloDTO> page) {
        return page.getContent().stream().map(ListaPrecioArticuloDTO::getId).collect(Collectors.toList());
    }

    private static List<Long> concat(List<Long> a, List<Long> b) {
        List<Long> r = new java.util.ArrayList<>(a);
        r.addAll(b);
        return r;
    }
}
