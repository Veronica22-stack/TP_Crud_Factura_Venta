package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacturaReporteDTO {
    private Long id;
    private Long numeroFactura;
    private Date fechaEmision;
    private String clienteDenominacion;
    private String condicionIva;
    private String puntoVentaDescripcion;
    private double importeTotal;
    private long cantidadItems;
    private String estado;
    private String observaciones;
    private List<FacturaDetalleReporteDTO> detalles;

    // Constructor para consultas JPQL previas (compatibilidad hacia atrás)
    public FacturaReporteDTO(Long numeroFactura, Date fechaEmision, String clienteDenominacion,
                             String condicionIva, String puntoVentaDescripcion, double importeTotal,
                             long cantidadItems) {
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.clienteDenominacion = clienteDenominacion;
        this.condicionIva = condicionIva;
        this.puntoVentaDescripcion = puntoVentaDescripcion;
        this.importeTotal = importeTotal;
        this.cantidadItems = cantidadItems;
    }

    // Constructor para consultas JPQL con ID y Estado
    public FacturaReporteDTO(Long id, Long numeroFactura, Date fechaEmision, String clienteDenominacion,
                             String condicionIva, String puntoVentaDescripcion, double importeTotal,
                             long cantidadItems, String estado) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.clienteDenominacion = clienteDenominacion;
        this.condicionIva = condicionIva;
        this.puntoVentaDescripcion = puntoVentaDescripcion;
        this.importeTotal = importeTotal;
        this.cantidadItems = cantidadItems;
        this.estado = estado;
    }
}
