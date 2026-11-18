package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.PuntoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntoVentaRepository extends JpaRepository<PuntoVenta, Long> {
}
