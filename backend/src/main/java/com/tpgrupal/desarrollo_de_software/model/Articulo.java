package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class Articulo extends AuditoriaApp{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubro_id")
    private Rubro rubro;
    @Column(name="codigo",nullable = false)
    private String codigo;
    @Column(name="denominacion",nullable = false)
    private String denominacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private Marca marca;
    
}
