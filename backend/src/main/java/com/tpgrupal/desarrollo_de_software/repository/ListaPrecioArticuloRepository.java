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

    @Query("SELECT lpa FROM ListaPrecioArticulo lpa " +
           "JOIN FETCH lpa.articulo a " +
           "LEFT JOIN FETCH a.rubro r " +
           "JOIN FETCH lpa.listaPrecio lp " +
           "WHERE (:listaPrecioId IS NULL OR lp.id = :listaPrecioId) " +
           "AND (:buscar IS NULL OR LOWER(a.denominacion) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
           "OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')))")
    List<ListaPrecioArticulo> buscarArticulos(
            @Param("buscar") String buscar,
            @Param("listaPrecioId") Long listaPrecioId);
    
    Optional<ListaPrecioArticulo> findByArticuloIdAndListaPrecioId(Long articuloId, Long listaPrecioId);
    
    Optional<ListaPrecioArticulo> findFirstByArticuloIdOrderByIdAsc(Long articuloId);
}
