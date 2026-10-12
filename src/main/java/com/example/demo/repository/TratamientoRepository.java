package com.example.demo.repository;

import com.example.demo.model.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
    List<Tratamiento> findByActivoTrue();
    List<Tratamiento> findByActivoTrueAndCategoria(String categoria);

    List<Tratamiento> findByActivoTrueOrderByNombreAsc();

    List<Tratamiento> findAllByOrderByCategoriaAscNombreAsc();

    List<Tratamiento> findByCategoriaOrderByNombreAsc(String categoria);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);

    long countByCategoriaIgnoreCase(String categoria);

    @Query("SELECT DISTINCT t.categoria FROM Tratamiento t " +
            "WHERE t.categoria IS NOT NULL AND TRIM(t.categoria) <> '' " +
            "ORDER BY t.categoria")
    List<String> findCategorias();
}
