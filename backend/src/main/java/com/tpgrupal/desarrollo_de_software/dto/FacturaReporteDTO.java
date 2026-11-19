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
    private String motivoRechazo;
    // Datos fiscales del detalle
    private Integer tipoComprobante;      // 1 = A, 6 = B, 11 = C
    private String clienteCuit;
    private Long condicionIvaId;
    private Long puntoVentaId;
    private Integer puntoVentaNumero;
    private Long tipoMonedaId;
    private String monedaCodigoAfip;
    private String monedaDenominacion;
    private Double importeNetoGravado;
    private Double importeIva;
    private Date fechaAnulacion;

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

    // Listado con los datos de ARCA (resultado, motivo del rechazo y tipo de comprobante)
    public FacturaReporteDTO(Long id, Long numeroFactura, Date fechaEmision, String clienteDenominacion,
                             String condicionIva, String puntoVentaDescripcion, double importeTotal,
                             long cantidadItems, String estado, String cae, Date caeFechaVencimiento,
                             String resultadoAfip, String motivoRechazo, Integer tipoComprobante) {
        this(id, numeroFactura, fechaEmision, clienteDenominacion, condicionIva, puntoVentaDescripcion,
                importeTotal, cantidadItems, estado, cae);
        this.caeFechaVencimiento = caeFechaVencimiento;
        this.resultadoAfip = resultadoAfip;
        this.motivoRechazo = motivoRechazo;
        this.tipoComprobante = tipoComprobante;
    }
}
