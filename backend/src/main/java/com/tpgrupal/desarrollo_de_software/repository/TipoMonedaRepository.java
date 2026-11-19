package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.TipoMoneda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMonedaRepository extends JpaRepository<TipoMoneda, Long> {
    Optional<TipoMoneda> findFirstByCodigoAfipIgnoreCase(String codigoAfip);
}
