package com.tambo.sistematambo.config;

import com.tambo.sistematambo.modulo.Modulo;
import com.tambo.sistematambo.modulo.ModuloRepository;
import com.tambo.sistematambo.perfil.Perfil;
import com.tambo.sistematambo.perfil.PerfilRepository;
import com.tambo.sistematambo.user.User;
import com.tambo.sistematambo.user.UserRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner seedDefaultUser(
            UserRepository userRepository,
            PerfilRepository perfilRepository,
            ModuloRepository moduloRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Set<Modulo> modulos = seedModules(moduloRepository);

            Perfil adminPerfil = perfilRepository.findByNombreIgnoreCase("Administrador")
                    .orElseGet(() -> {
                        Perfil perfil = new Perfil();
                        perfil.setNombre("Administrador");
                        perfil.setDescripcion("Acceso completo al sistema");
                        perfil.setEstado(true);
                        return perfilRepository.save(perfil);
                    });

            adminPerfil.setModulos(modulos);
            adminPerfil = perfilRepository.save(adminPerfil);

            User admin = userRepository.findByCorreoOrUsuario("admin@tambo.com", "admin")
                    .orElseGet(() -> userRepository.save(
                            new User(
                                    "Administrador",
                                    "admin",
                                    passwordEncoder.encode("Admin123*"),
                                    "admin@tambo.com",
                                    1)));

            if (admin.getPerfil() == null) {
                admin.setPerfil(adminPerfil);
                userRepository.save(admin);
            }
        };
    }

    private Set<Modulo> seedModules(ModuloRepository moduloRepository) {
        Map<String, String> modulosBase = new LinkedHashMap<>();
        modulosBase.put("Dashboard", "/dashboard");
        modulosBase.put("Gestion Categorias", "/dashboard/categorias");
        modulosBase.put("Gestion Productos", "/dashboard/productos");
        modulosBase.put("Gestion Clientes", "/dashboard/clientes");
        modulosBase.put("Gestion Ventas", "/dashboard/ventas");
        modulosBase.put("Inventario por Ubicacion", "/dashboard/inventario");
        modulosBase.put("Gestion Almacenes", "/dashboard/almacenes");
        modulosBase.put("Pedidos Internos", "/dashboard/pedidos");
        modulosBase.put("Distribucion y Rutas", "/dashboard/rutas");
        modulosBase.put("Gestion Usuarios", "/dashboard/usuarios");
        modulosBase.put("Gestion Perfiles", "/dashboard/perfiles");

        Set<Modulo> modulos = new LinkedHashSet<>();

        for (Map.Entry<String, String> entry : modulosBase.entrySet()) {
            Modulo modulo = moduloRepository.findAll().stream()
                    .filter(item -> item.getNombre().equalsIgnoreCase(entry.getKey()))
                    .findFirst()
                    .orElseGet(() -> {
                        Modulo nuevo = new Modulo();
                        nuevo.setNombre(entry.getKey());
                        nuevo.setRuta(entry.getValue());
                        nuevo.setIcono(null);
                        return moduloRepository.save(nuevo);
                    });

            modulos.add(modulo);
        }

        return modulos;
    }
}
