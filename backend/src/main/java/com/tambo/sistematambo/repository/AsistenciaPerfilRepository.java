package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.AsistenciaPerfil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaPerfilRepository extends JpaRepository<AsistenciaPerfil, Long> {
    Optional<AsistenciaPerfil> findByCodigoIgnoreCase(String codigo);
    Optional<AsistenciaPerfil> findByUsuarioId(Long usuarioId);
}
