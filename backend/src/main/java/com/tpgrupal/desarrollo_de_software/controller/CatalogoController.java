package com.tpgrupal.desarrollo_de_software.controller;

import com.tpgrupal.desarrollo_de_software.dto.ClienteDTO;
import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioArticuloDTO;
import com.tpgrupal.desarrollo_de_software.dto.PageResponse;
import com.tpgrupal.desarrollo_de_software.service.BusquedaCatalogoService;
import com.tpgrupal.desarrollo_de_software.service.FacturacionAfipService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogos para los formularios: búsquedas paginadas de productos y clientes, monedas y tipos de comprobante. */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final BusquedaCatalogoService busquedaCatalogoService;
    private final FacturacionAfipService facturacionAfipService;

    public CatalogoController(BusquedaCatalogoService busquedaCatalogoService,
                              FacturacionAfipService facturacionAfipService) {
        this.busquedaCatalogoService = busquedaCatalogoService;
        this.facturacionAfipService = facturacionAfipService;
    }

    // Explorador de productos: palabras en código, denominación, rubro, marca o lista (sin importar tildes)
    @GetMapping("/productos")
    public PageResponse<ListaPrecioArticuloDTO> buscarProductos(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanio) {
        return busquedaCatalogoService.buscarProductos(buscar, listaPrecioId, pagina, tamanio);
    }

    // Buscador de clientes: por CUIT/CUIL (con o sin guiones) y/o razón social
    @GetMapping("/clientes/buscar")
    public PageResponse<ClienteDTO> buscarClientes(
            @RequestParam(required = false) String cuit,
            @RequestParam(required = false) String razonSocial,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanio) {
        return busquedaCatalogoService.buscarClientes(cuit, razonSocial, pagina, tamanio);
    }

    @GetMapping("/monedas")
    public List<Map<String, Object>> monedas() {
        return facturacionAfipService.monedas();
    }

    @GetMapping("/tipos-comprobante")
    public List<Map<String, Object>> tiposComprobante() {
        return List.of(
                tipo(1, "Factura A", "Discrimina IVA. Requiere cliente con CUIT."),
                tipo(6, "Factura B", "IVA incluido en el precio (se informa el IVA contenido)."),
                tipo(11, "Factura C", "No discrimina IVA (monotributo / exento)."));
    }

    private Map<String, Object> tipo(int codigo, String nombre, String descripcion) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", codigo);
        m.put("nombre", nombre);
        m.put("descripcion", descripcion);
        m.put("porDefecto", codigo == facturacionAfipService.tipoComprobantePorDefecto());
        return m;
    }
}
