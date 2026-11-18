package com.tpgrupal.desarrollo_de_software.auth;

import com.tpgrupal.desarrollo_de_software.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Revisa cada pedido a /api/** ANTES de que llegue al controller.
 * Si no trae un token válido, responde 401 y el pedido no sigue.
 * Se registra en WebConfig (excepto /api/auth/login, que es público).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Las consultas previas de CORS (OPTIONS) las manda el navegador sin token: se dejan pasar
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Optional<Long> usuarioId = authService.validarToken(obtenerToken(request));
        if (usuarioId.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\","
                    + "\"message\":\"Sesión inválida o vencida. Inicie sesión nuevamente.\"}");
            return false;
        }

        SesionActual.setUsuarioId(usuarioId.get());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SesionActual.limpiar();
    }

    // El token viene en el encabezado "Authorization: Bearer <token>".
    // Los links de PDF y Excel se abren en otra pestaña y no pueden mandar encabezados: ahí viene como ?token=...
    private String obtenerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return request.getParameter("token");
    }
}
