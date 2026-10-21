package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Usuario extends EntityId{
    @Column(name="usuario",nullable = false)
    private String usuario;

    @Column(name="clave",nullable = false)
    private String clave;

    @Column(name="nombre",nullable = false)
    private String nombre;

    @Column(name="apellido",nullable = false)
    private String apellido;

}
