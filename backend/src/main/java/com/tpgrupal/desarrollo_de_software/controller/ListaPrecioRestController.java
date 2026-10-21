package com.tpgrupal.desarrollo_de_software.controller;

import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioArticuloDTO;
import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioDTO;
import com.tpgrupal.desarrollo_de_software.service.ListaPrecioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ListaPrecioRestController {

    private final ListaPrecioService listaPrecioService;

    public ListaPrecioRestController(ListaPrecioService listaPrecioService) {
        this.listaPrecioService = listaPrecioService;
    }

    @GetMapping("/listas-precio")
    public ResponseEntity<List<ListaPrecioDTO>> listarListasPrecio() {
        return ResponseEntity.ok(listaPrecioService.listarListasPrecio());
    }

    @GetMapping("/lista-precio-articulos")
    public ResponseEntity<List<ListaPrecioArticuloDTO>> buscarArticulos(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long listaPrecioId) {
        return ResponseEntity.ok(listaPrecioService.buscarArticulos(buscar, listaPrecioId));
    }

    @GetMapping("/puntos-venta")
    public ResponseEntity<List<Map<String, Object>>> listarPuntosVenta() {
        return ResponseEntity.ok(listaPrecioService.listarPuntosVenta());
    }

    @GetMapping("/condiciones-iva")
    public ResponseEntity<List<Map<String, Object>>> listarCondicionesIva() {
        return ResponseEntity.ok(listaPrecioService.listarCondicionesIva());
    }
}
