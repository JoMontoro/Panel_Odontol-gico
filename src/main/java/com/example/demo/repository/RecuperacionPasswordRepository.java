package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.RecuperacionPassword;

public interface RecuperacionPasswordRepository extends JpaRepository<RecuperacionPassword, Integer> {

    /** Codigo vigente mas reciente del correo (aun sin usar y sin agotar intentos). */
    Optional<RecuperacionPassword> findFirstByCorreoIgnoreCaseAndUsadoFalseOrderByIdDesc(String correo);

    List<RecuperacionPassword> findByCorreoIgnoreCaseAndUsadoFalse(String correo);

    void deleteByCorreoIgnoreCaseAndUsadoFalse(String correo);
}
