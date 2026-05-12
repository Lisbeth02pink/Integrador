package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.PedidoInterno;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoInternoRepository extends JpaRepository<PedidoInterno, Long> {

    @EntityGraph(attributePaths = {"tienda", "items", "items.producto"})
    List<PedidoInterno> findAllByOrderByFechaSolicitudDesc();
}
