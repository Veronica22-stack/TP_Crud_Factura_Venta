package com.tpgrupal.desarrollo_de_software.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class EntityId {
    @Id
    //GeneratedValue me genera el Id automaticamente al insertar en Base de datos
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
}
