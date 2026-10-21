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
public class TipoMoneda extends AuditoriaApp{
    @Column(name="codigo_afip",nullable = false)
    private String codigoAfip;
    @Column(name="denominacion",nullable = false)
    private String denominacion;
    @Column(name="simbolo",nullable = false)
    private String simbolo;

}
