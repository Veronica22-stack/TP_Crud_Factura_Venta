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
    private String productoDescripcion;
    private double cantidad;
    private double precioUnitario;
    private double subtotal;
}
