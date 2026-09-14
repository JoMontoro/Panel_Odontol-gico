package com.example.demo.repository;

import com.example.demo.model.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
    List<Tratamiento> findByActivoTrue();
    List<Tratamiento> findByActivoTrueAndCategoria(String categoria);
}