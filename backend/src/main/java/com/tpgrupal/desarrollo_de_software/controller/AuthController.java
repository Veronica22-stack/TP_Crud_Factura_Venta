package com.tpgrupal.desarrollo_de_software.controller;

import com.tpgrupal.desarrollo_de_software.dto.LoginRequestDTO;
import com.tpgrupal.desarrollo_de_software.dto.LoginResponseDTO;
import com.tpgrupal.desarrollo_de_software.dto.RegistroRequestDTO;
import com.tpgrupal.desarrollo_de_software.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Público: valida usuario y clave y devuelve el token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        Optional<LoginResponseDTO> resultado = authService.login(request);
        if (resultado.isEmpty()) {
            // Si el usuario no existe se avisa, para que el frontend ofrezca registrarlo
            if (request != null && !authService.existeUsuario(request.getUsuario())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", 401, "message", "El usuario no existe.", "usuarioInexistente", true));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Usuario o contraseña incorrectos."));
        }
        return ResponseEntity.ok(resultado.get());
    }

    // Público: crea un usuario nuevo y devuelve el token (queda con la sesión iniciada)
    @PostMapping("/registro")
    public ResponseEntity<LoginResponseDTO> registrar(@RequestBody RegistroRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    // Protegido por AuthInterceptor: borra la sesión del token recibido
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7).trim());
        }
        return ResponseEntity.noContent().build();
    }
}
