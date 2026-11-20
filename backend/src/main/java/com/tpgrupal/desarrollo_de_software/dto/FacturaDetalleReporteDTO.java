package com.tpgrupal.desarrollo_de_software.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaDetalleReporteDTO {
    private Long id;
    private Long articuloId;
    // Lista de precios de la que salió el precio de este ítem (cada ítem puede venir de una lista distinta)
    private Long listaPrecioArticuloId;
    private Long listaPrecioId;
    private String productoDescripcion;
    private double cantidad;
    private double precioUnitario;
    private double subtotal;
    private String codigo;
    private String rubro;
    private double porcentajeBonificacion;
    private double importeNeto;
    private double importeIva;
}
