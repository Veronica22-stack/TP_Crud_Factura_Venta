package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Totales del panel de control, calculados en la base (COUNT / SUM). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenFacturacionDTO {
    private double totalFacturado;   // suma de las facturas no anuladas
    private long vigentes;           // no anuladas
    private long conCae;             // no anuladas y con CAE
    private long pendientes;         // no anuladas y sin CAE
    private long anuladas;
    private List<FacturaReporteDTO> ultimasFacturas;
}
