package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class ListaPrecio extends AuditoriaApp{
    @Column(name="codigo",nullable = false)
    private String codigo;
    @Column(name="denominacion",nullable = false)
    private String denominacion;

}
