package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Domicilio extends EntityId{
        @Column(name="nombre_calle")
        private String nombreCalle;
        @Column(name="numero_calle")
        private String numeroCalle;

}
