package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.ProveedorEntrega;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorEntregaRepository extends JpaRepository<ProveedorEntrega, Long> {

    @EntityGraph(attributePaths = {"proveedor", "producto", "almacenDestino"})
    List<ProveedorEntrega> findAllByOrderByFechaEntregaDesc();
}
