package com.example.demo.repository;

import com.example.demo.model.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {

    /**
     * Catalogo propio de cada odontologo: :odontoId es el odontologo conectado
     * y -1 (SesionActual.SIN_FILTRO) cuando el usuario es el ADMIN supervisor,
     * unico que consulta el catalogo completo de los dos.
     */
    String CATALOGO = "(:odontoId = -1 or t.odontologo.id = :odontoId)";

    @Query("SELECT t FROM Tratamiento t WHERE t.activo = true and " + CATALOGO +
            " ORDER BY t.nombre ASC")
    List<Tratamiento> activosDe(@Param("odontoId") Integer odontoId);

    @Query("SELECT t FROM Tratamiento t WHERE (:incluirInactivos = true or t.activo = true) " +
            "and " + CATALOGO + " ORDER BY t.categoria ASC, t.nombre ASC")
    List<Tratamiento> todosDe(@Param("odontoId") Integer odontoId,
                              @Param("incluirInactivos") boolean incluirInactivos);

    @Query("SELECT t FROM Tratamiento t WHERE t.categoria = :categoria and " + CATALOGO +
            " ORDER BY t.nombre ASC")
    List<Tratamiento> porCategoriaDe(@Param("odontoId") Integer odontoId,
                                     @Param("categoria") String categoria);

    @Query("SELECT DISTINCT t.categoria FROM Tratamiento t " +
            "WHERE t.categoria IS NOT NULL AND TRIM(t.categoria) <> '' " +
            "and " + CATALOGO + " ORDER BY t.categoria")
    List<String> categoriasDe(@Param("odontoId") Integer odontoId);

    /** Tratamientos heredados de la version compartida, todavia sin dueno. */
    List<Tratamiento> findByOdontologoIsNull();

    /** El codigo es unico dentro del catalogo propio: cada odontologo puede usar los mismos codigos. */
    boolean existsByCodigoIgnoreCaseAndOdontologo_Id(String codigo, Integer odontoId);

    boolean existsByCodigoIgnoreCaseAndOdontologo_IdAndIdNot(String codigo, Integer odontoId, Integer id);
}
