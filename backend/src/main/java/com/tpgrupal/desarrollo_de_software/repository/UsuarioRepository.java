package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca el usuario por su nombre de usuario (el que se escribe en el login)
    Optional<Usuario> findByUsuario(String usuario);
}
