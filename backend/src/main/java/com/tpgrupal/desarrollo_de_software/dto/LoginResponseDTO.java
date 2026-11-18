package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que devuelve el backend si el login es correcto (nunca se devuelve la clave)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String usuario;
    private String nombre;
    private String apellido;
}
