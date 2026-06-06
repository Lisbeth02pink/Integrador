package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.Merma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MermaRepository extends JpaRepository<Merma, Long> {
}
