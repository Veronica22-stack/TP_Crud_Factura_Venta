package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que manda el frontend al iniciar sesión: POST /api/auth/login
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String usuario;
    private String clave;
}
