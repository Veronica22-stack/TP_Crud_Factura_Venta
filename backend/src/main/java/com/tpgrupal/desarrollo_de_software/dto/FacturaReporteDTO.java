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
    private Long clienteId;
    private String clienteDenominacion;
    private String condicionIva;
    private String puntoVentaDescripcion;
    private double importeTotal;
    private long cantidadItems;
    private String estado;
    private String observaciones;
    private Long listaPrecioId;
    private List<FacturaDetalleReporteDTO> detalles;
    private String cae;
    private Date caeFechaVencimiento;
    private String resultadoAfip;
    private Integer nroComprobanteAfip;
    private Integer tipoComprobanteAfip;

    // Constructor que usa la consulta JPQL del listado (SELECT new ...)
    public FacturaReporteDTO(Long id, Long numeroFactura, Date fechaEmision, String clienteDenominacion,
                             String condicionIva, String puntoVentaDescripcion, double importeTotal,
                             long cantidadItems, String estado, String cae) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.clienteDenominacion = clienteDenominacion;
        this.condicionIva = condicionIva;
        this.puntoVentaDescripcion = puntoVentaDescripcion;
        this.importeTotal = importeTotal;
        this.cantidadItems = cantidadItems;
        this.estado = estado;
        this.cae = cae;
    }
}
