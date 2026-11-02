package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaPrecioArticuloDTO {
    private Long id;
    private Long articuloId;
    private String codigo;
    private String denominacion;
    private String rubro;
    private Double precioVenta;
    private String listaPrecioNombre;
}
