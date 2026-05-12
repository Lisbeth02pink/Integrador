package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.Categoria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    Optional<Categoria> findByCodigo(String codigo);
}
