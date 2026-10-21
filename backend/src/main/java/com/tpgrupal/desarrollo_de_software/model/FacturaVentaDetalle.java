package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"factura"}) // Lombok excluye la referencia a la factura
//con exclude evito el stackoverflow error
@EqualsAndHashCode(callSuper = true, exclude = {"factura"})
public class FacturaVentaDetalle extends EntityId {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_venta_id", nullable = false)
    private FacturaVenta factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_precio_articulo_id", nullable = false)
    private ListaPrecioArticulo listaPrecioArticulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private double cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private double precioUnitario;

    @Column(name = "porcentaje_ue_bonificacion")
    private double porcentajeBonificacion;
    @Column(name = "importe_neto")
    private double importeNeto;
    @Column(name = "importe_iva")
    private double importeIva;

    @Column(name = "subtotal", nullable = false)
    private double importeSubtotal;
}
