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

    // Datos para AFIP. Si no vienen, FacturacionAfipService los completa.
    private Integer ptoVta;
    private Integer cbteTipo;             // 1 = Factura A, 6 = Factura B, 11 = Factura C
    private Integer concepto;             // 1 = Productos, 2 = Servicios, 3 = Productos y Servicios
    private Integer docTipo;              // 80 = CUIT, 96 = DNI, 99 = Sin identificar
    private Long docNro;
    private Integer condicionIvaReceptor; // 1 = RI, 4 = Exento, 5 = Consumidor Final, 6 = Monotributo
    private Double alicuotaIvaPct;    // solo Factura A y B
    private String fchServDesde;      // yyyyMMdd, solo servicios
    private String fchServHasta;
    private String fchVtoPago;

    public Long getNumeroFactura() {
        if (numeroFactura != null) return numeroFactura;
        return numero;
    }
}
