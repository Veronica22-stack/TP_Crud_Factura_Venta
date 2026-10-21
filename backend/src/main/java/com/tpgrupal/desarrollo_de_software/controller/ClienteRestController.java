package com.tpgrupal.desarrollo_de_software.controller;

import com.tpgrupal.desarrollo_de_software.dto.ClienteDTO;
import com.tpgrupal.desarrollo_de_software.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    private final ClienteService clienteService;

    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes(@RequestParam(required = false) String buscar) {
        return ResponseEntity.ok(clienteService.buscarClientes(buscar));
    }
}
