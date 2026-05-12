package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.VentaResumen;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaResumenRepository extends JpaRepository<VentaResumen, Long> {
    List<VentaResumen> findByFechaBetweenOrderByFechaDesc(LocalDate desde, LocalDate hasta);
}
