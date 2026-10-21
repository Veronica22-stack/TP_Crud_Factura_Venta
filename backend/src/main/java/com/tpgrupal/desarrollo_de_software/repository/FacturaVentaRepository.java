package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.FacturaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaVentaRepository extends JpaRepository<FacturaVenta, Long> {
    Optional<FacturaVenta> findByNumero(Long numero);

    boolean existsByNumero(Long numero);

    @Query("SELECT COALESCE(MAX(f.numero), 1000) FROM FacturaVenta f")
    Long findMaxNumero();
}
