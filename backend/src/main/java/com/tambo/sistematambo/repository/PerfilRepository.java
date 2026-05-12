package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.Perfil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    Optional<Perfil> findByNombreIgnoreCase(String nombre);
}
