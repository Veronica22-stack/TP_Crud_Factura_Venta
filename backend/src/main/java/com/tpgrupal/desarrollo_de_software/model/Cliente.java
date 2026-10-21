package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Cliente extends AuditoriaApp{
    @Column(name="cut_cuil",nullable = false)
    private String cuitCuil;
    @Column(name="denominacion",nullable = false)
    private String denominacion;
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "domicilio_id", nullable = false)
    private Domicilio domicilio;
}
