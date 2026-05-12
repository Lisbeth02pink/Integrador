package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.AsistenciaRegistro;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRegistroRepository extends JpaRepository<AsistenciaRegistro, Long> {
    List<AsistenciaRegistro> findAllByFechaBetweenOrderByFechaAsc(LocalDateTime inicio, LocalDateTime fin);
    List<AsistenciaRegistro> findByUsuarioIdAndFechaBetweenOrderByFechaAsc(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);
}
