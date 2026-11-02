package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:buscar IS NULL OR LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
           "OR c.cuitCuil LIKE CONCAT('%', :buscar, '%'))")
    List<Cliente> buscarClientes(@Param("buscar") String buscar);
}
