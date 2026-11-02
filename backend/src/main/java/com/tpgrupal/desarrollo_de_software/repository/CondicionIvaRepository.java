package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.CondicionIva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CondicionIvaRepository extends JpaRepository<CondicionIva, Long> {
    Optional<CondicionIva> findByDenominacionIgnoreCase(String denominacion);
}
