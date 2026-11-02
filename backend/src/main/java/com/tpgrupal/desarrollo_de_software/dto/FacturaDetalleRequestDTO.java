package com.tpgrupal.desarrollo_de_software.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaDetalleRequestDTO {
    private Long id;

    private Long articuloId;
    private Long listaPrecioArticuloId;

    private String productoDescripcion;
    private String descripcion;
    private Double cantidad;
    private Double precioUnitario;
    private Double porcentajeBonificacion;
    private Double importeNeto;
    private Double importeIva;
    private Double subtotal;
}
