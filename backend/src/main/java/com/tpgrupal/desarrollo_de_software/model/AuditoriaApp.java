package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaApp extends EntityId{
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false)
        protected Date fechaAlta;
        
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = true)
        protected Date fechaBaja;
        
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = true)
        protected Date fechaModificacion;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name ="usuario_carga_id", nullable = false)
        protected Usuario usuarioCarga;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "usuario_baja_id")
        protected Usuario usuarioBaja;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "usuario_modificacion_id", nullable = false)
        protected Usuario usuarioModificacion;
        
        //de esta forma evito tener que settear todos estos atributos cada vez que doy de alta un nuevo objeto
        @PrePersist
        protected void prePersist() {
                Date ahora = new Date();
                if (fechaAlta == null) fechaAlta = ahora;
                if (fechaModificacion == null) fechaModificacion = ahora;
                if (usuarioModificacion == null) usuarioModificacion = usuarioCarga;
        }
}
