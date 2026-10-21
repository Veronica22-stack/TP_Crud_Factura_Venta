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
public class PuntoVenta extends AuditoriaApp{
    @Column (name="numero",nullable = false)
    private int numero;
    @Column(name="descripcion")
    private String descripcion;
    @Column(name="tipo_emision")
    private String tipoEmision;
    @Column(name="domicilio_comercial")
    private String domicilioComercial;

}
