package com.tpgrupal.desarrollo_de_software.controller;

import com.tpgrupal.desarrollo_de_software.service.FacturacionAfipService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Estado y diagnóstico de la integración con ARCA, y catálogo de monedas. */
@RestController
@RequestMapping("/api/afip")
public class AfipController {

    private final FacturacionAfipService facturacionAfipService;

    public AfipController(FacturacionAfipService facturacionAfipService) {
        this.facturacionAfipService = facturacionAfipService;
    }

    // Modo, CUIT, credenciales y URLs configuradas
    @GetMapping("/estado")
    public Map<String, Object> estado() {
        return facturacionAfipService.estado();
    }

    // Prueba de conexión con WSFEv1 (FEDummy)
    @PostMapping("/dummy")
    public Map<String, Object> probarConexion() {
        return facturacionAfipService.probarConexion();
    }

    @GetMapping("/monedas")
    public List<Map<String, Object>> monedas() {
        return facturacionAfipService.monedas();
    }

    // Actualiza el catálogo de monedas con FEParamGetTiposMonedas
    @PostMapping("/monedas/sincronizar")
    public Map<String, Object> sincronizarMonedas() {
        return facturacionAfipService.sincronizarMonedas();
    }
}
