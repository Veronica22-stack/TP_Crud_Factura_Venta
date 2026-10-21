package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class ListaPrecioArticulo extends AuditoriaApp{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_precio_id", nullable = false)
    private ListaPrecio listaPrecio;
    @Column(name="precio_venta",nullable = false)
    private double precioVenta;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;
    
}
