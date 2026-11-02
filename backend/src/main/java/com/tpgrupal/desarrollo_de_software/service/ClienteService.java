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

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarClientes(String query) {
        return clienteRepository.buscarClientes(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
