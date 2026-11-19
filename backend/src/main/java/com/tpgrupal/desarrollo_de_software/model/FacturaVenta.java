package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"detalles"})
@ToString(exclude = {"detalles"})//lombok excluye detalles
//con exclude evito el stackoverflow error
public class FacturaVenta extends AuditoriaApp {
    @Column(unique = true) // la base rechaza números de factura repetidos
    private Long numero;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_emision", nullable = false)
    private Date fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    @Column(name = "importe_cobrado")
    private double importeCobrado;
    @Column(name = "importe_saldo")
    private double importeSaldo;

    @Column(name = "importe_total", nullable = false)
    private double importeTotal;

    @Column(name = "cae")
    private String cae;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_vto")
    private Date caeFechaVencimiento;
    @Column(name = "resultado_afip")
    private String resultadoAfip;
    @Column(name = "nro_comprobante_afip")
    private Integer nroComprobanteAfip;
    @Column(name = "tipo_comprobante_afip")
    private Integer tipoComprobanteAfip;
    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    // Tipo de comprobante de ARCA: 1 = Factura A, 6 = Factura B, 11 = Factura C
    @Column(name = "tipo_comprobante")
    private Integer tipoComprobante;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_anulacion")
    private Date fechaAnulacion;
    @Column(name = "observaciones")
    private String observaciones;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_moneda", nullable = false)
    private TipoMoneda tipoMoneda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condicion_iva", nullable = false)
    private CondicionIva condicionIva;

    // Neto gravado e IVA de toda la factura: no se guardan (no hay columna), se calculan con calcularTotales()
    @Transient
    private double importeNetoGravado;
    @Transient
    private double importeIvaTotal;

    public static final double ALICUOTA_IVA = 21.0;

    public boolean discriminaIva() {
        return tipoComprobante != null && (tipoComprobante == 1 || tipoComprobante == 6);
    }

    /**
     * Recalcula cada renglón (subtotal con bonificación, neto e IVA) y los totales de la factura.
     * En Factura A y B el neto es subtotal / 1,21; en Factura C no se discrimina IVA.
     */
    public void calcularTotales() {
        boolean discrimina = discriminaIva();
        double total = 0, neto = 0, iva = 0;
        for (FacturaVentaDetalle d : detalles) {
            double subtotal = redondear(d.getCantidad() * d.getPrecioUnitario()
                    * (1 - d.getPorcentajeBonificacion() / 100.0));
            double netoRenglon = discrimina ? redondear(subtotal / (1 + ALICUOTA_IVA / 100.0)) : subtotal;
            d.setImporteSubtotal(subtotal);
            d.setImporteNeto(netoRenglon);
            d.setImporteIva(redondear(subtotal - netoRenglon));
            total += subtotal;
            neto += d.getImporteNeto();
            iva += d.getImporteIva();
        }
        importeTotal = redondear(total);
        importeNetoGravado = redondear(neto);
        importeIvaTotal = redondear(iva);
        importeSaldo = redondear(importeTotal - importeCobrado);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // Metodo de conveniencia para asociación bidireccional
    public void addDetalle(FacturaVentaDetalle detalle) {
        detalles.add(detalle);
        detalle.setFactura(this);
    }
}

