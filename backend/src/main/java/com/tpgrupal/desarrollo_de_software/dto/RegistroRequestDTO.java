package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que manda el frontend para crear un usuario nuevo: POST /api/auth/registro
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequestDTO {
    private String usuario;
    private String clave;
    private String nombre;
    private String apellido;
}
