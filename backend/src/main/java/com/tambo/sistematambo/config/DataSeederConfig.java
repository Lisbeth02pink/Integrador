package com.tambo.sistematambo.config;

import com.tambo.sistematambo.model.Almacen;
import com.tambo.sistematambo.model.Categoria;
import com.tambo.sistematambo.model.Cliente;
import com.tambo.sistematambo.model.DetalleVenta;
import com.tambo.sistematambo.model.EmpleadoControl;
import com.tambo.sistematambo.model.Modulo;
import com.tambo.sistematambo.model.MovimientoInventario;
import com.tambo.sistematambo.model.PedidoInterno;
import com.tambo.sistematambo.model.PedidoInternoItem;
import com.tambo.sistematambo.model.Perfil;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.model.Proveedor;
import com.tambo.sistematambo.model.RutaEntrega;
import com.tambo.sistematambo.model.Transferencia;
import com.tambo.sistematambo.model.TransferenciaDetalle;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.model.Venta;
import com.tambo.sistematambo.model.VentaResumen;
import com.tambo.sistematambo.repository.AlmacenRepository;
import com.tambo.sistematambo.repository.CategoriaRepository;
import com.tambo.sistematambo.repository.ClienteRepository;
import com.tambo.sistematambo.repository.EmpleadoControlRepository;
import com.tambo.sistematambo.repository.ModuloRepository;
import com.tambo.sistematambo.repository.MovimientoInventarioRepository;
import com.tambo.sistematambo.repository.PerfilRepository;
import com.tambo.sistematambo.repository.PedidoInternoRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.repository.ProveedorRepository;
import com.tambo.sistematambo.repository.RutaEntregaRepository;
import com.tambo.sistematambo.repository.TransferenciaRepository;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.repository.VentaRepository;
import com.tambo.sistematambo.repository.VentaResumenRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner seedDefaultUser(
            UserRepository userRepository,
            ClienteRepository clienteRepository,
            ProveedorRepository proveedorRepository,
            PerfilRepository perfilRepository,
            ModuloRepository moduloRepository,
            CategoriaRepository categoriaRepository,
            AlmacenRepository almacenRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            PedidoInternoRepository pedidoInternoRepository,
            RutaEntregaRepository rutaEntregaRepository,
            TransferenciaRepository transferenciaRepository,
            VentaRepository ventaRepository,
            VentaResumenRepository ventaResumenRepository,
            EmpleadoControlRepository empleadoControlRepository,
            JdbcTemplate jdbcTemplate,
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

            seedClientes(clienteRepository);
            seedProveedores(proveedorRepository);
            cleanupOrphanProductReferences(jdbcTemplate);
            cleanupOrphanUserReferences(jdbcTemplate);
            seedOperaciones(
                    categoriaRepository,
                    almacenRepository,
                    productoRepository,
                    movimientoInventarioRepository,
                    pedidoInternoRepository,
                    rutaEntregaRepository,
                    transferenciaRepository,
                    ventaRepository,
                    ventaResumenRepository,
                    empleadoControlRepository);
        };
    }

    private void cleanupOrphanProductReferences(JdbcTemplate jdbcTemplate) {
        deleteIfTableExists(jdbcTemplate, "pedidos_internos_items", "producto_id");
        deleteIfTableExists(jdbcTemplate, "detalle_ventas", "producto_id");
        deleteIfTableExists(jdbcTemplate, "movimientos_inventario", "producto_id");
        deleteIfTableExists(jdbcTemplate, "transferencias_detalle", "producto_id");
        deleteIfTableExists(jdbcTemplate, "proveedores_entregas", "producto_id");
    }

    private void cleanupOrphanUserReferences(JdbcTemplate jdbcTemplate) {
        nullIfTableExists(jdbcTemplate, "asistencia_registros", "usuario_id", "usuarios");
        nullIfTableExists(jdbcTemplate, "asistencia_perfiles", "usuario_id", "usuarios");
    }

    private void deleteIfTableExists(JdbcTemplate jdbcTemplate, String tableName, String productColumn) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        if (exists != null && exists > 0) {
            jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + productColumn + " NOT IN (SELECT id FROM productos)");
        }
    }

    private void nullIfTableExists(JdbcTemplate jdbcTemplate, String tableName, String columnName, String targetTable) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        if (exists != null && exists > 0) {
            jdbcTemplate.update("UPDATE " + tableName + " SET " + columnName + " = NULL WHERE " + columnName + " IS NOT NULL AND " + columnName + " NOT IN (SELECT id FROM " + targetTable + ")");
        }
    }

    private Set<Modulo> seedModules(ModuloRepository moduloRepository) {
        Map<String, String> modulosBase = new LinkedHashMap<>();
        modulosBase.put("Dashboard", "/dashboard");
        modulosBase.put("Gestion Asistencia", "/dashboard/asistencia");
        modulosBase.put("Gestion Categorias", "/dashboard/categorias");
        modulosBase.put("Gestion Productos", "/dashboard/productos");
        modulosBase.put("Gestion Clientes", "/dashboard/clientes");
        modulosBase.put("Gestion Ventas", "/dashboard/ventas");
        modulosBase.put("Gestion Proveedores", "/dashboard/proveedores");
        modulosBase.put("Gestion Tiendas", "/dashboard/tiendas");
        modulosBase.put("Inventario por Ubicacion", "/dashboard/inventario");
        modulosBase.put("Gestion Almacenes", "/dashboard/almacenes");
        modulosBase.put("Pedidos Internos", "/dashboard/pedidos");
        modulosBase.put("Transferencias Logisticas", "/dashboard/transferencias");
        modulosBase.put("Distribucion y Rutas", "/dashboard/rutas");
        modulosBase.put("Gestion Reportes", "/dashboard/reportes");
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

    private void seedClientes(ClienteRepository clienteRepository) {
        if (clienteRepository.existsByDocumento("74352618")) {
            return;
        }

        Cliente clienteDni = new Cliente();
        clienteDni.setTipoDocumento("DNI");
        clienteDni.setDocumento("74352618");
        clienteDni.setNombre("ANA TORRES");
        clienteDni.setTelefono("987654321");
        clienteDni.setCorreo("ana@correo.com");
        clienteDni.setDireccion("Lima");
        clienteDni.setEstado(1);
        clienteRepository.save(clienteDni);

        Cliente clienteRuc = new Cliente();
        clienteRuc.setTipoDocumento("RUC");
        clienteRuc.setDocumento("20123456789");
        clienteRuc.setNombre("COMERCIAL TAMBO SAC");
        clienteRuc.setTelefono("945612378");
        clienteRuc.setCorreo("ventas@tambo.com");
        clienteRuc.setDireccion("Arequipa");
        clienteRuc.setEstado(1);
        clienteRepository.save(clienteRuc);
    }

    private void seedProveedores(ProveedorRepository proveedorRepository) {
        Proveedor proveedorBebidas = proveedorRepository.findAll().stream()
                .filter(item -> "20111111111".equals(item.getRuc()))
                .findFirst()
                .orElseGet(Proveedor::new);
        proveedorBebidas.setRuc("20111111111");
        proveedorBebidas.setRazonSocial("DISTRIBUIDORA BEBIDAS LIMA SAC");
        proveedorBebidas.setContacto("Carla Mendoza");
        proveedorBebidas.setTelefono("987321654");
        proveedorBebidas.setCorreo("abastecimiento@bebidaslima.pe");
        proveedorBebidas.setDireccion("Av. Argentina 1450, Lima");
        proveedorBebidas.setProductosSuministrados("Agua, gaseosas, energizantes");
        proveedorBebidas.setHistorialEntregas("4 entregas al almacen central en el ultimo mes");
        proveedorBebidas.setEstado(1);
        proveedorRepository.save(proveedorBebidas);

        Proveedor proveedorConsumo = proveedorRepository.findAll().stream()
                .filter(item -> "20444444444".equals(item.getRuc()))
                .findFirst()
                .orElseGet(Proveedor::new);
        proveedorConsumo.setRuc("20444444444");
        proveedorConsumo.setRazonSocial("CONSUMO MASIVO NORTE SAC");
        proveedorConsumo.setContacto("Ruben Salazar");
        proveedorConsumo.setTelefono("955888221");
        proveedorConsumo.setCorreo("ventas@consumonorte.pe");
        proveedorConsumo.setDireccion("Jr. Paruro 845, Lima");
        proveedorConsumo.setProductosSuministrados("Snacks, galletas, limpieza y descartables");
        proveedorConsumo.setHistorialEntregas("2 entregas programadas en la semana");
        proveedorConsumo.setEstado(1);
        proveedorRepository.save(proveedorConsumo);
    }

    private void seedOperaciones(
            CategoriaRepository categoriaRepository,
            AlmacenRepository almacenRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            PedidoInternoRepository pedidoInternoRepository,
            RutaEntregaRepository rutaEntregaRepository,
            TransferenciaRepository transferenciaRepository,
            VentaRepository ventaRepository,
            VentaResumenRepository ventaResumenRepository,
            EmpleadoControlRepository empleadoControlRepository) {
        Categoria bebidas = ensureCategoria(
                categoriaRepository,
                List.of("CAT-BEB"),
                "Bebidas",
                "CAT-BEB",
                "Gaseosas, aguas, jugos y energizantes",
                "https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=500&q=80",
                1);

        Categoria snacks = ensureCategoria(
                categoriaRepository,
                List.of("CAT-SNK", "CAT-TEC"),
                "Snacks",
                "CAT-SNK",
                "Galletas, golosinas y snacks salados de alta rotacion",
                "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?auto=format&fit=crop&w=500&q=80",
                1);

        Categoria limpieza = ensureCategoria(
                categoriaRepository,
                List.of("CAT-LIM", "CAT-ROP"),
                "Limpieza",
                "CAT-LIM",
                "Papel higienico, limpieza y cuidado del punto de venta",
                "https://images.unsplash.com/photo-1583947582886-f40ec95dd752?auto=format&fit=crop&w=500&q=80",
                1);

        Almacen lima = ensureAlmacen(
                almacenRepository,
                List.of("Almacen Central Lima", "Almacen Lima"),
                "Almacen Central Lima",
                "Lima",
                "Rosa Calderon",
                "Av. Javier Prado 1480",
                "CENTRAL",
                1000,
                760,
                1);

        Almacen chiclayo = ensureAlmacen(
                almacenRepository,
                List.of("Tienda Tambo Chiclayo Centro", "Almacen Chiclayo"),
                "Tienda Tambo Chiclayo Centro",
                "Chiclayo",
                "Luis Paredes",
                "Av. Balta 922",
                "TIENDA",
                640,
                420,
                1);

        Almacen trujillo = ensureAlmacen(
                almacenRepository,
                List.of("Tienda Tambo Trujillo Real Plaza", "Almacen Trujillo"),
                "Tienda Tambo Trujillo Real Plaza",
                "Trujillo",
                "Mila Torres",
                "Jr. Pizarro 311",
                "TIENDA",
                520,
                178,
                1);

        Producto agua = ensureProducto(
                productoRepository,
                List.of("BEB-001"),
                "Agua Cielo 625ml",
                "BEB-001",
                "https://images.unsplash.com/photo-1564419434663-c499673ea48f?auto=format&fit=crop&w=400&q=80",
                new BigDecimal("1.10"),
                new BigDecimal("2.50"),
                18,
                20,
                1,
                bebidas,
                lima);

        Producto inkaKola = ensureProducto(
                productoRepository,
                List.of("SNK-104", "TEC-104"),
                "Inka Kola 500ml",
                "SNK-104",
                "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=400&q=80",
                new BigDecimal("2.00"),
                new BigDecimal("3.50"),
                42,
                12,
                1,
                bebidas,
                lima);

        Producto papelHigienico = ensureProducto(
                productoRepository,
                List.of("LIM-205", "ROP-205"),
                "Papel Higienico Elite 4 und",
                "LIM-205",
                "https://images.unsplash.com/photo-1604335399105-a0c585fd81a1?auto=format&fit=crop&w=400&q=80",
                new BigDecimal("8.50"),
                new BigDecimal("12.90"),
                27,
                10,
                1,
                limpieza,
                chiclayo);

        Producto galletas = ensureProducto(
                productoRepository,
                List.of("SNK-222", "TEC-222"),
                "Galletas Oreo Clasicas",
                "SNK-222",
                "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=400&q=80",
                new BigDecimal("1.80"),
                new BigDecimal("2.80"),
                64,
                18,
                1,
                snacks,
                trujillo);

        if (movimientoInventarioRepository.count() == 0) {
            movimientoInventarioRepository.save(createMovimiento(
                    LocalDateTime.of(2026, 4, 25, 8, 10),
                    agua,
                    "Egreso",
                    12,
                    chiclayo,
                    null,
                    "Venta en tienda TIC-1802"));
            movimientoInventarioRepository.save(createMovimiento(
                    LocalDateTime.of(2026, 4, 24, 17, 42),
                    inkaKola,
                    "Ingreso",
                    15,
                    null,
                    lima,
                    "Recepcion proveedor OC-903"));
            movimientoInventarioRepository.save(createMovimiento(
                    LocalDateTime.of(2026, 4, 24, 13, 15),
                    papelHigienico,
                    "Transferencia",
                    8,
                    lima,
                    chiclayo,
                    "Despacho interno TR-101"));
        }

        if (pedidoInternoRepository.count() == 0) {
            pedidoInternoRepository.save(createPedidoInterno(
                    chiclayo,
                    "Luis Paredes",
                    "Alta",
                    LocalDateTime.of(2026, 5, 5, 9, 20),
                    "Preparando",
                    "Reposicion urgente por alta rotacion de bebidas.",
                    List.of(
                            createPedidoInternoItem(agua, 30),
                            createPedidoInternoItem(inkaKola, 6))));

            pedidoInternoRepository.save(createPedidoInterno(
                    trujillo,
                    "Mila Torres",
                    "Media",
                    LocalDateTime.of(2026, 5, 5, 11, 5),
                    "En ruta",
                    "Abastecimiento programado de snacks y venta rapida.",
                    List.of(createPedidoInternoItem(galletas, 8))));
        }

        if (rutaEntregaRepository.count() == 0) {
            List<PedidoInterno> pedidos = pedidoInternoRepository.findAll();
            PedidoInterno pedidoChiclayo = pedidos.isEmpty() ? null : pedidos.get(0);
            PedidoInterno pedidoTrujillo = pedidos.size() > 1 ? pedidos.get(1) : null;
            rutaEntregaRepository.save(createRuta(
                    "Ruta Norte 01",
                    "Los Olivos - SMP",
                    "Jorge Meza",
                    pedidoChiclayo,
                    "Camion 3T",
                    "ABC-123",
                    LocalDate.of(2026, 5, 6),
                    13,
                    "En ruta",
                    "08:20",
                    58));
            rutaEntregaRepository.save(createRuta(
                    "Ruta Centro 03",
                    "Lince - Jesus Maria",
                    "Camila Vela",
                    pedidoTrujillo,
                    "Van refrigerada",
                    "BCD-234",
                    LocalDate.of(2026, 5, 6),
                    8,
                    "Pendiente",
                    "10:00",
                    14));
            rutaEntregaRepository.save(createRuta(
                    "Ruta Sur 02",
                    "Surco - Chorrillos",
                    "Marco Ponce",
                    null,
                    "Moto carga",
                    "CDE-345",
                    LocalDate.of(2026, 5, 5),
                    17,
                    "Entregado",
                    "07:45",
                    100));
        }

        if (transferenciaRepository.count() == 0) {
            Transferencia transferencia = new Transferencia();
            transferencia.setFecha(LocalDateTime.of(2026, 5, 5, 12, 30));
            transferencia.setAlmacenOrigen(lima);
            transferencia.setAlmacenDestino(chiclayo);
            transferencia.setEstado("COMPLETADA");
            transferencia.setResponsable("Rosa Calderon");
            transferencia.setReferencia("Transferencia inicial de abastecimiento");

            TransferenciaDetalle detalle = new TransferenciaDetalle();
            detalle.setTransferencia(transferencia);
            detalle.setProducto(agua);
            detalle.setCantidad(30);
            transferencia.getDetalles().add(detalle);
            transferenciaRepository.save(transferencia);
        }

        if (ventaRepository.count() == 0) {
            Venta venta = new Venta();
            venta.setCliente(null);
            venta.setFecha(LocalDateTime.of(2026, 5, 5, 18, 10));
            venta.setMetodoPago("TARJETA");
            venta.setAlmacen(chiclayo);
            venta.setUsuario(null);
            venta.setTotal(new BigDecimal("75.00"));

            venta.getDetalles().add(createDetalleVenta(venta, agua, 10, "2.50"));
            venta.getDetalles().add(createDetalleVenta(venta, galletas, 8, "2.80"));
            ventaRepository.save(venta);
        }

        if (ventaResumenRepository.count() == 0) {
            ventaResumenRepository.save(createVenta(LocalDate.of(2026, 4, 25), "Tienda", "4850.00", "1920.00", "Agua Cielo 625ml"));
            ventaResumenRepository.save(createVenta(LocalDate.of(2026, 4, 24), "Delivery", "3980.00", "1640.00", "Inka Kola 500ml"));
            ventaResumenRepository.save(createVenta(LocalDate.of(2026, 4, 23), "Marketplace", "5240.00", "2150.00", "Galletas Oreo Clasicas"));
        }

        if (empleadoControlRepository.count() == 0) {
            empleadoControlRepository.save(createEmpleado("Ana Delgado", "Cajera", "08:02", "17:00", 1, 0, 24, "Presente"));
            empleadoControlRepository.save(createEmpleado("Piero Salas", "Almacenero", "08:21", "17:00", 4, 1, 21, "Tarde"));
            empleadoControlRepository.save(createEmpleado("Diana Flores", "Repartidora", "--", "--", 0, 2, 20, "Falta"));
        }
    }

    private Almacen ensureAlmacen(
            AlmacenRepository almacenRepository,
            List<String> aliases,
            String nombre,
            String ciudad,
            String responsable,
            String direccion,
            String tipo,
            Integer capacidad,
            Integer ocupacion,
            Integer estado) {
        Almacen almacen = aliases.stream()
                .map(almacenRepository::findByNombre)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(Almacen::new);

        almacen.setNombre(nombre);
        almacen.setCiudad(ciudad);
        almacen.setResponsable(responsable);
        almacen.setDireccion(direccion);
        almacen.setTipo(tipo);
        almacen.setCapacidad(capacidad);
        almacen.setOcupacion(ocupacion);
        almacen.setEstado(estado);
        return almacenRepository.save(almacen);
    }

    private Categoria ensureCategoria(
            CategoriaRepository categoriaRepository,
            List<String> aliases,
            String nombre,
            String codigo,
            String descripcion,
            String imagen,
            Integer estado) {
        Categoria categoria = aliases.stream()
                .map(categoriaRepository::findByCodigo)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(Categoria::new);

        categoria.setNombre(nombre);
        categoria.setCodigo(codigo);
        categoria.setDescripcion(descripcion);
        categoria.setImagen(imagen);
        categoria.setEstado(estado);
        return categoriaRepository.save(categoria);
    }

    private Producto ensureProducto(
            ProductoRepository productoRepository,
            List<String> aliases,
            String nombre,
            String sku,
            String imagen,
            BigDecimal precioCompra,
            BigDecimal precioVenta,
            Integer stock,
            Integer stockMinimo,
            Integer estado,
            Categoria categoria,
            Almacen almacen) {
        Producto producto = aliases.stream()
                .map(alias -> productoRepository.findBySkuAndAlmacenId(alias, almacen.getId()))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(Producto::new);

        producto.setNombre(nombre);
        producto.setSku(sku);
        producto.setImagen(imagen);
        producto.setPrecioCompra(precioCompra);
        producto.setPrecioVenta(precioVenta);
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setEstado(estado);
        producto.setCategoria(categoria);
        producto.setAlmacen(almacen);
        return productoRepository.save(producto);
    }

    private Producto createProducto(
            String nombre,
            String sku,
            String imagen,
            BigDecimal precioCompra,
            BigDecimal precioVenta,
            Integer stock,
            Integer stockMinimo,
            Integer estado,
            Categoria categoria,
            Almacen almacen) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setSku(sku);
        producto.setImagen(imagen);
        producto.setPrecioCompra(precioCompra);
        producto.setPrecioVenta(precioVenta);
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setEstado(estado);
        producto.setCategoria(categoria);
        producto.setAlmacen(almacen);
        return producto;
    }

    private MovimientoInventario createMovimiento(
            LocalDateTime fecha,
            Producto producto,
            String tipo,
            Integer cantidad,
            Almacen origen,
            Almacen destino,
            String referencia) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFecha(fecha);
        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setAlmacenOrigen(origen);
        movimiento.setAlmacenDestino(destino);
        movimiento.setReferencia(referencia);
        return movimiento;
    }

    private PedidoInterno createPedidoInterno(
            Almacen tienda,
            String solicitadoPor,
            String prioridad,
            LocalDateTime fechaSolicitud,
            String estado,
            String observaciones,
            List<PedidoInternoItem> items) {
        PedidoInterno pedido = new PedidoInterno();
        pedido.setTienda(tienda);
        pedido.setSolicitadoPor(solicitadoPor);
        pedido.setPrioridad(prioridad);
        pedido.setFechaSolicitud(fechaSolicitud);
        pedido.setEstado(estado);
        pedido.setObservaciones(observaciones);

        for (PedidoInternoItem item : items) {
            item.setPedidoInterno(pedido);
            pedido.getItems().add(item);
        }

        return pedido;
    }

    private PedidoInternoItem createPedidoInternoItem(Producto producto, Integer cantidad) {
        PedidoInternoItem item = new PedidoInternoItem();
        item.setProducto(producto);
        item.setCantidad(cantidad);
        return item;
    }

    private RutaEntrega createRuta(
            String nombre,
            String zona,
            String repartidor,
            PedidoInterno pedidoInterno,
            String vehiculo,
            String placa,
            LocalDate fechaEntrega,
            Integer pedidos,
            String estado,
            String horaSalida,
            Integer progreso) {
        RutaEntrega ruta = new RutaEntrega();
        ruta.setNombre(nombre);
        ruta.setZona(zona);
        ruta.setRepartidor(repartidor);
        ruta.setPedidoInterno(pedidoInterno);
        ruta.setVehiculo(vehiculo);
        ruta.setPlaca(placa);
        ruta.setFechaEntrega(fechaEntrega);
        ruta.setPedidos(pedidos);
        ruta.setEstado(estado);
        ruta.setHoraSalida(horaSalida);
        ruta.setProgreso(progreso);
        return ruta;
    }

    private DetalleVenta createDetalleVenta(Venta venta, Producto producto, Integer cantidad, String precio) {
        DetalleVenta detalle = new DetalleVenta();
        BigDecimal precioUnitario = new BigDecimal(precio);
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecio(precioUnitario);
        detalle.setSubtotal(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
        return detalle;
    }

    private VentaResumen createVenta(
            LocalDate fecha,
            String canal,
            String ingresos,
            String egresos,
            String productoMasVendido) {
        VentaResumen venta = new VentaResumen();
        venta.setFecha(fecha);
        venta.setCanal(canal);
        venta.setIngresos(new BigDecimal(ingresos));
        venta.setEgresos(new BigDecimal(egresos));
        venta.setProductoMasVendido(productoMasVendido);
        return venta;
    }

    private EmpleadoControl createEmpleado(
            String nombre,
            String cargo,
            String entrada,
            String salida,
            Integer tardanzas,
            Integer faltas,
            Integer asistencias,
            String estado) {
        EmpleadoControl empleado = new EmpleadoControl();
        empleado.setNombre(nombre);
        empleado.setCargo(cargo);
        empleado.setEntrada(entrada);
        empleado.setSalida(salida);
        empleado.setTardanzas(tardanzas);
        empleado.setFaltas(faltas);
        empleado.setAsistencias(asistencias);
        empleado.setEstado(estado);
        return empleado;
    }
}
