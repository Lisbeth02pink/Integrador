package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.Almacen;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    Optional<Almacen> findByNombre(String nombre);
}
