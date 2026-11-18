package com.tpgrupal.desarrollo_de_software.service;

import com.tpgrupal.desarrollo_de_software.dto.LoginRequestDTO;
import com.tpgrupal.desarrollo_de_software.dto.LoginResponseDTO;
import com.tpgrupal.desarrollo_de_software.dto.RegistroRequestDTO;
import com.tpgrupal.desarrollo_de_software.exception.DuplicateResourceException;
import com.tpgrupal.desarrollo_de_software.model.Usuario;
import com.tpgrupal.desarrollo_de_software.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    // La sesión dura 8 horas desde que se inicia
    private static final Duration DURACION_SESION = Duration.ofHours(8);

    // Sesiones abiertas: token -> sesión. Viven en memoria: si se reinicia el backend, hay que volver a entrar.
    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private record Sesion(Long usuarioId, Instant vence) {
    }

    /**
     * Valida usuario y clave. Si son correctos crea una sesión y devuelve el token.
     * Si la clave estaba guardada en texto plano (datos viejos), la encripta con BCrypt la primera vez.
     */
    @Transactional
    public Optional<LoginResponseDTO> login(LoginRequestDTO request) {
        if (request == null || request.getUsuario() == null || request.getClave() == null) {
            return Optional.empty();
        }
        Optional<Usuario> encontrado = usuarioRepository.findByUsuario(request.getUsuario().trim());
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        Usuario u = encontrado.get();
        if (!claveCorrecta(u, request.getClave())) {
            return Optional.empty();
        }

        return Optional.of(abrirSesion(u));
    }

    // Crea la sesión del usuario y arma la respuesta con el token
    private LoginResponseDTO abrirSesion(Usuario u) {
        String token = UUID.randomUUID().toString();
        sesiones.put(token, new Sesion(u.getId(), Instant.now().plus(DURACION_SESION)));

        return LoginResponseDTO.builder()
                .token(token)
                .usuario(u.getUsuario())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .build();
    }

    // Indica si ya hay un usuario con ese nombre (lo usa el login para ofrecer el registro)
    public boolean existeUsuario(String usuario) {
        return usuario != null && usuarioRepository.findByUsuario(usuario.trim()).isPresent();
    }

    /**
     * Registra un usuario nuevo (clave encriptada con BCrypt) y deja la sesión iniciada.
     */
    @Transactional
    public LoginResponseDTO registrar(RegistroRequestDTO request) {
        if (request == null || estaVacio(request.getUsuario()) || estaVacio(request.getClave())
                || estaVacio(request.getNombre()) || estaVacio(request.getApellido())) {
            throw new IllegalArgumentException("Complete usuario, contraseña, nombre y apellido.");
        }
        if (request.getClave().length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }
        if (existeUsuario(request.getUsuario())) {
            throw new DuplicateResourceException("Ya existe un usuario con ese nombre.");
        }

        Usuario u = new Usuario();
        u.setUsuario(request.getUsuario().trim());
        u.setClave(encoder.encode(request.getClave()));
        u.setNombre(request.getNombre().trim());
        u.setApellido(request.getApellido().trim());
        usuarioRepository.save(u);

        return abrirSesion(u);
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean claveCorrecta(Usuario u, String claveIngresada) {
        String guardada = u.getClave();
        if (guardada == null) {
            return false;
        }
        if (esBCrypt(guardada)) {
            return encoder.matches(claveIngresada, guardada);
        }
        // Clave vieja en texto plano: se compara y, si coincide, se guarda encriptada
        if (guardada.equals(claveIngresada)) {
            u.setClave(encoder.encode(claveIngresada));
            usuarioRepository.save(u);
            return true;
        }
        return false;
    }

    private static boolean esBCrypt(String clave) {
        return clave.startsWith("$2a$") || clave.startsWith("$2b$") || clave.startsWith("$2y$");
    }

    // Devuelve el id del usuario dueño del token, o vacío si no existe o ya venció
    public Optional<Long> validarToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(sesion.vence())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(sesion.usuarioId());
    }

    public void logout(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }
}
