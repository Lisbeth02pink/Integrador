package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsBySkuAndAlmacenId(String sku, Long almacenId);
    boolean existsBySkuAndAlmacenIdAndIdNot(String sku, Long almacenId, Long id);
    Optional<Producto> findBySkuAndAlmacenId(String sku, Long almacenId);
    List<Producto> findAllByAlmacenId(Long almacenId);
}
