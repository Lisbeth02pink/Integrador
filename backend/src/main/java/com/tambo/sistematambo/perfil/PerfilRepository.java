package com.tambo.sistematambo.perfil;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Perfil> findByNombreIgnoreCase(String nombre);
}
