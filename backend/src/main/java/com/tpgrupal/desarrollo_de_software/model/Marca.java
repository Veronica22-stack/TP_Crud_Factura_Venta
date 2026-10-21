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
public class Marca extends AuditoriaApp{
    @Column(name="denominacion",nullable = false)
    private String denominacion;
    @Column(name="codigo",nullable = false)
    private Integer codigo;
    
}
