package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.dto.ListaPrecioDTO;
import com.tpgrupal.desarrollo_de_software.repository.CondicionIvaRepository;
import com.tpgrupal.desarrollo_de_software.repository.ListaPrecioArticuloRepository;
import com.tpgrupal.desarrollo_de_software.repository.ListaPrecioRepository;
import com.tpgrupal.desarrollo_de_software.repository.PuntoVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListaPrecioService {

    private final ListaPrecioRepository listaPrecioRepository;
    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final CondicionIvaRepository condicionIvaRepository;

    public ListaPrecioService(ListaPrecioRepository listaPrecioRepository,
                              ListaPrecioArticuloRepository listaPrecioArticuloRepository,
                              PuntoVentaRepository puntoVentaRepository,
                              CondicionIvaRepository condicionIvaRepository) {
        this.listaPrecioRepository = listaPrecioRepository;
        this.listaPrecioArticuloRepository = listaPrecioArticuloRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.condicionIvaRepository = condicionIvaRepository;
    }

    public List<ListaPrecioDTO> listarListasPrecio() {
        return listaPrecioRepository.findAll().stream()
                .map(lp -> ListaPrecioDTO.builder()
                        .id(lp.getId())
                        .codigo(lp.getCodigo())
                        .denominacion(lp.getDenominacion())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarPuntosVenta() {
        return puntoVentaRepository.findAll().stream()
                .map(pv -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pv.getId());
                    map.put("numero", pv.getNumero());
                    map.put("descripcion", pv.getDescripcion());
                    map.put("domicilioComercial", pv.getDomicilioComercial());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarCondicionesIva() {
        return condicionIvaRepository.findAll().stream()
                .map(ci -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", ci.getId());
                    map.put("codigoAfip", ci.getCodigoAfip());
                    map.put("denominacion", ci.getDenominacion());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
