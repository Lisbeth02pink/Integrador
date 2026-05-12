package com.tambo.sistematambo.repository;

import com.tambo.sistematambo.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCorreoOrUsuario(String correo, String usuario);
    boolean existsByUsuarioIgnoreCase(String usuario);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, Long id);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    long countByPerfilId(Long perfilId);
}
