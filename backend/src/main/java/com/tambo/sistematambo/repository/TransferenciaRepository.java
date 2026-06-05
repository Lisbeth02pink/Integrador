package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.Transferencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    List<Transferencia> findAllByOrderByFechaDesc();
}
