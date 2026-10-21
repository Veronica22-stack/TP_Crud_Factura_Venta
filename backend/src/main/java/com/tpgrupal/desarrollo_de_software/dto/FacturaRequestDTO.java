package com.tpgrupal.desarrollo_de_software.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaRequestDTO {
    private Long id;
    private Long numero;
    private Long numeroFactura;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date fechaEmision;

    private Long clienteId;
    private Long condicionIvaId;
    private Long puntoVentaId;
    private Long tipoMonedaId;
    private Long listaPrecioId;
    private String listaPrecioNombre;

    private String clienteDenominacion;
    private String condicionIva;
    private String puntoVentaDescripcion;

    private String estado;
    private String observaciones;
    private Double importeTotal;

    @Builder.Default
    private List<FacturaDetalleRequestDTO> detalles = new ArrayList<>();

    public Long getNumeroFactura() {
        if (numeroFactura != null) return numeroFactura;
        return numero;
    }
}
