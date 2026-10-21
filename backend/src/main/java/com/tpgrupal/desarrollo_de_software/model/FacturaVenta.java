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
    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

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
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_moneda", nullable = false)
    private TipoMoneda tipoMoneda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condicion_iva", nullable = false)
    private CondicionIva condicionIva;

    // Metodo de conveniencia para asociación bidireccional
    public void addDetalle(FacturaVentaDetalle detalle) {
        detalles.add(detalle);
        detalle.setFactura(this);
    }
}

