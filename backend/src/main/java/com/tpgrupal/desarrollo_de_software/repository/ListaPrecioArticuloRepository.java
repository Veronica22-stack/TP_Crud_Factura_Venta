package com.tpgrupal.desarrollo_de_software.repository;

import com.tpgrupal.desarrollo_de_software.model.ListaPrecioArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaPrecioArticuloRepository extends JpaRepository<ListaPrecioArticulo, Long> {

    Optional<ListaPrecioArticulo> findByArticuloIdAndListaPrecioId(Long articuloId, Long listaPrecioId);
    
    Optional<ListaPrecioArticulo> findFirstByArticuloIdOrderByIdAsc(Long articuloId);
}
