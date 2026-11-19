package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.dto.ClienteDTO;
import com.tpgrupal.desarrollo_de_software.model.Cliente;
import com.tpgrupal.desarrollo_de_software.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final BusquedaCatalogoService busquedaCatalogoService;

    public ClienteService(ClienteRepository clienteRepository, BusquedaCatalogoService busquedaCatalogoService) {
        this.clienteRepository = clienteRepository;
        this.busquedaCatalogoService = busquedaCatalogoService;
    }

    // Busca por razón social (sin importar mayúsculas ni tildes) o por CUIT con o sin guiones
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarClientes(String query) {
        return busquedaCatalogoService.buscarClientesPorTexto(query);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> obtenerTodos() {
        return clienteRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ClienteDTO mapToDto(Cliente c) {
        String email = (c.getContacto() != null) ? c.getContacto().getEmail() : "-";
        String tel = (c.getContacto() != null) ? c.getContacto().getTelefono() : "-";

        return ClienteDTO.builder()
                .id(c.getId())
                .cuitCuil(c.getCuitCuil())
                .denominacion(c.getDenominacion())
                .email(email)
                .telefono(tel)
                .build();
    }
}
