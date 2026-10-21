package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Contacto extends EntityId{
    @Column(name="email")
    private String email;
    @Column(name="telefono")
    private String telefono;
    @Column(name="celular")
    private String celular;

}
