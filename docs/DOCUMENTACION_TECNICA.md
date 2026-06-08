# Documentación Técnica del Sistema Logístico Tambo

## 1. Información general del proyecto

**Nombre del sistema:** Sistema Logístico Tambo  
**Tipo de sistema:** Sistema web para gestión logística  
**Empresa de referencia:** Tiendas Tambo  
**Arquitectura:** Cliente - Servidor  
**Backend:** Java con Spring Boot  
**Frontend:** Angular  
**Base de datos:** MySQL  
**Contenedores:** Docker y Docker Compose  
**Documentación:** Markdown  
**Modelado de procesos:** BPMN.io  

---

## 2. Descripción general del sistema

El Sistema Logístico Tambo es una solución web orientada a automatizar los procesos logísticos relacionados con el abastecimiento, control de inventario, pedidos internos, transferencias y distribución de productos hacia las tiendas.

El sistema se enfoca principalmente en el control del almacén central, permitiendo registrar entregas de proveedores, administrar productos, controlar stock disponible, atender solicitudes de reposición de tiendas, generar transferencias y supervisar rutas de entrega.

La solución está dividida en dos componentes principales:

- **Backend:** desarrollado en Java con Spring Boot, encargado de gestionar la lógica de negocio, seguridad, conexión con la base de datos y servicios REST.
- **Frontend:** desarrollado en Angular, encargado de presentar la interfaz de usuario y consumir los servicios del backend.

Además, el sistema utiliza Docker para facilitar la ejecución de los servicios en contenedores.

---

## 3. Objetivo del sistema

Automatizar la gestión logística de Tambo mediante un sistema web que permita controlar el abastecimiento de productos, inventario, pedidos internos, transferencias y rutas de entrega, mejorando la eficiencia operativa y reduciendo errores en los procesos manuales.

---

## 4. Alcance del sistema

El sistema permite gestionar los siguientes procesos:

- Registro y administración de proveedores.
- Registro y administración de productos.
- Gestión de categorías.
- Gestión de almacenes y tiendas.
- Control de inventario.
- Registro de movimientos de inventario.
- Registro de entregas de proveedores al almacén central.
- Gestión de pedidos internos de reposición.
- Gestión de transferencias desde almacén central hacia tiendas.
- Gestión de rutas de entrega.
- Gestión de usuarios, perfiles y permisos.
- Control de autenticación y seguridad.
- Visualización de reportes operativos.

---

## 5. Tecnologías utilizadas

| Componente | Tecnología |
|---|---|
| Backend | Java / Spring Boot |
| Frontend | Angular |
| Base de datos | MySQL |
| Seguridad | Spring Security / JWT |
| Contenedores | Docker |
| Orquestación | Docker Compose |
| Editor de código | Visual Studio Code |
| Modelado de procesos | BPMN.io |
| Documentación técnica | Markdown |
| Control de versiones | Git / GitHub |

---

## 6. Estructura general del proyecto

```text
sistematambo/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com/tambo/sistematambo/
│   │               ├── auth/
│   │               ├── config/
│   │               ├── controller/
│   │               ├── dto/
│   │               ├── model/
│   │               ├── repository/
│   │               └── service/
│   │
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── auth/
│   │   │   │   └── services/
│   │   │   ├── features/
│   │   │   │   ├── auth/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── operations/
│   │   │   │   └── layout/
│   │   │   └── shared/
│   │   ├── assets/
│   │   └── environments/
│   │
│   ├── angular.json
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml
│
└── docs/
    └── DOCUMENTACION_TECNICA.md
```

---

## 7. Arquitectura del sistema

El sistema utiliza una arquitectura cliente-servidor.

```text
Usuario
  ↓
Frontend Angular
  ↓
Servicios HTTP
  ↓
Backend Spring Boot
  ↓
Base de datos MySQL
```

### 7.1 Frontend

El frontend es la capa visual del sistema. Permite que los usuarios interactúen con los módulos de proveedores, productos, inventario, pedidos internos, transferencias, rutas, usuarios y reportes.

### 7.2 Backend

El backend procesa las solicitudes enviadas desde el frontend. Contiene controladores, DTO, modelos, servicios, repositorios, configuración de seguridad y lógica de negocio.

### 7.3 Base de datos

La base de datos almacena toda la información del sistema: productos, almacenes, proveedores, pedidos, transferencias, rutas, usuarios, perfiles y movimientos de inventario.

### 7.4 Docker

Docker permite ejecutar el backend, frontend y base de datos mediante contenedores, facilitando la instalación y despliegue del sistema.

---

### 7.5 Patrones y principios aplicados

El proyecto aplica una arquitectura por capas compatible con MVC y con separacion de responsabilidades:

| Evidencia | Ubicacion | Aplicacion |
|---|---|---|
| MVC / arquitectura por capas | `controller`, `service`, `model`, `frontend/src/app/features` | Los controladores reciben solicitudes REST, los servicios contienen reglas de negocio, los modelos representan entidades y Angular actua como vista del sistema. |
| DAO / Repository | `backend/src/main/java/com/tambo/sistematambo/repository` | Las interfaces `JpaRepository` encapsulan el acceso a datos y evitan que controladores o vistas consulten directamente la base de datos. |
| SOLID - responsabilidad unica | `controller`, `service`, `repository`, `dto`, `response`, `security` | Cada paquete tiene una responsabilidad delimitada: entrada HTTP, negocio, persistencia, contratos de datos, salida segura y seguridad. |
| SOLID - inversion de dependencias | Constructores de servicios y controladores | Las clases dependen de abstracciones inyectadas por Spring, por ejemplo repositorios, servicios de token y `PasswordEncoder`. |
| Seguridad probada | `src/test/java/com/tambo/sistematambo/security` | Existen pruebas para hash de refresh token y proteccion de endpoints de reportes. |

### 7.6 Uso justificado de librerias Java

| Libreria | Uso en el sistema | Valor aportado |
|---|---|---|
| Apache POI | `ReporteExcelService` | Genera reportes Excel `.xlsx` con hojas, estilos, encabezados y descarga desde endpoints REST. |
| Logback | `logback-spring.xml` | Centraliza logs en consola, archivo general y archivo especializado para reportes. |
| Apache Commons Lang | `AuthService`, `ProductoService`, `ReporteExcelService` | Normaliza cadenas, valida textos vacios y ayuda a enmascarar correos en reportes. |
| Google Guava | `RefreshTokenHashService` y `AuthService` | Construye listas inmutables de modulos y calcula SHA-256 del refresh token antes de guardarlo. |

Medidas de seguridad asociadas al tratamiento de informacion:

- Los reportes Excel requieren autenticacion y rol `ADMINISTRADOR`.
- El reporte de usuarios no exporta contrasenas ni refresh tokens.
- Los correos se exportan enmascarados en el reporte de usuarios.
- Los refresh tokens se almacenan como hash SHA-256 y se comparan sin exponer el valor plano.

---

## 8. Estructura del backend

El backend se encuentra en la carpeta:

```text
sistematambo/backend
```

### 8.1 Paquete `auth`

Contiene la lógica relacionada con autenticación, inicio de sesión, cierre de sesión, renovación de token y control de acceso.

Archivos principales:

- `AuthController.java`
- Clases relacionadas con login, logout y refresh token.

### 8.2 Paquete `config`

Contiene la configuración general del backend.

Archivos principales:

- `SecurityConfig.java`: configuración de seguridad.
- `WebConfig.java`: configuración web y CORS.
- `DataSeederConfig.java`: carga inicial de datos.

### 8.3 Paquete `controller`

Contiene los controladores REST que reciben las peticiones del frontend.

Controladores principales:

- `AlmacenController.java`
- `AsistenciaController.java`
- `AuthController.java`
- `CategoriaController.java`
- `ClienteController.java`
- `InventarioController.java`
- `ModuloController.java`
- `PedidoInternoController.java`
- `PerfilController.java`
- `ProductoController.java`
- `ProveedorController.java`
- `ProveedorEntregaController.java`
- `RutaEntregaController.java`
- `UserController.java`
- `VentaController.java`

### 8.4 Paquete `dto`

Contiene los objetos de transferencia de datos. Estos objetos se utilizan para enviar y recibir información entre el frontend y backend sin exponer directamente las entidades del sistema.

Ejemplos:

- `AlmacenRequest.java`
- `AuthRequest.java`
- `CategoriaRequest.java`
- `ClienteRequest.java`
- `PedidoInternoRequest.java`
- `PedidoInternoItemRequest.java`
- `ProductoRequest.java`
- `PerfilPermisosRequest.java`


### 8.4.1 DTO de tipo Request

Los archivos de tipo `Request` representan los datos que el frontend envía al backend cuando el usuario realiza una operación en el sistema, como registrar, editar, iniciar sesión, crear un pedido interno o generar una transferencia.

Estos DTO permiten recibir únicamente los campos necesarios para cada proceso, evitando exponer directamente las entidades de la base de datos.

#### Requests principales del sistema

| Archivo Request | Descripción |
|---|---|
| `AuthRequest.java` | Recibe las credenciales del usuario para el inicio de sesión. |
| `AuthRefreshRequest.java` | Recibe el token necesario para renovar la sesión del usuario. |
| `AuthLogoutRequest.java` | Recibe la información necesaria para cerrar sesión. |
| `AlmacenRequest.java` | Recibe los datos necesarios para registrar o actualizar un almacén o tienda. |
| `CategoriaRequest.java` | Recibe los datos de una categoría de productos. |
| `ClienteRequest.java` | Recibe los datos de un cliente, en caso el módulo de ventas se encuentre activo. |
| `ProductoRequest.java` | Recibe los datos necesarios para registrar o actualizar productos. |
| `PedidoInternoRequest.java` | Recibe los datos generales de un pedido interno de reposición. |
| `PedidoInternoItemRequest.java` | Recibe el detalle de productos y cantidades solicitadas en un pedido interno. |
| `PedidoInternoEstadoRequest.java` | Recibe el nuevo estado de un pedido interno, como aprobado, pendiente o rechazado. |
| `PerfilPermisosRequest.java` | Recibe la configuración de permisos asignados a un perfil. |
| `AsistenciaPerfilRequest.java` | Recibe información relacionada con perfiles de asistencia. |
| `AsistenciaRegistroRequest.java` | Recibe información de registros de asistencia. |

#### Ejemplo de Request para registrar un producto

```json
{
  "nombre": "Gaseosa personal",
  "sku": "PROD-001",
  "precioCompra": 2.50,
  "precioVenta": 3.50,
  "stock": 100,
  "stockMinimo": 20,
  "categoriaId": 1,
  "almacenId": 1,
  "estado": "ACTIVO"
}
```

#### Ejemplo de Request para registrar un pedido interno

```json
{
  "tiendaId": 2,
  "prioridad": "Alta",
  "solicitadoPor": "Encargado de tienda",
  "observaciones": "Reposición por stock bajo",
  "items": [
    {
      "productoId": 1,
      "cantidad": 50
    },
    {
      "productoId": 3,
      "cantidad": 30
    }
  ]
}
```

#### Ejemplo de Request para actualizar el estado de un pedido

```json
{
  "estado": "APROBADO",
  "observaciones": "Pedido aprobado por disponibilidad de stock"
}
```

---

### 8.4.2 DTO de tipo Response

Los archivos o estructuras de tipo `Response` representan los datos que el backend devuelve al frontend después de procesar una solicitud.

En el sistema, las respuestas pueden devolverse mediante DTO de salida o mediante las entidades controladas por los servicios REST. En caso de manejar una carpeta específica de `response`, esta debe documentar los objetos devueltos por cada módulo.

Los `Response` permiten entregar al frontend información clara, ordenada y segura, evitando mostrar datos sensibles como contraseñas, tokens internos o información innecesaria de la base de datos.

#### Responses principales recomendados

| Response | Descripción |
|---|---|
| `AuthResponse` | Devuelve la información del usuario autenticado y el token de acceso. |
| `UsuarioResponse` | Devuelve información básica del usuario, perfil y estado. |
| `ProductoResponse` | Devuelve los datos del producto, categoría, almacén y stock disponible. |
| `AlmacenResponse` | Devuelve información del almacén o tienda. |
| `ProveedorResponse` | Devuelve los datos del proveedor registrado. |
| `PedidoInternoResponse` | Devuelve la información del pedido interno, estado e items solicitados. |
| `TransferenciaResponse` | Devuelve la información de la transferencia, origen, destino y detalle de productos. |
| `RutaEntregaResponse` | Devuelve la información de la ruta, vehículo, repartidor, estado e incidencias. |
| `MensajeResponse` | Devuelve mensajes generales de confirmación o error. |

#### Ejemplo de Response de autenticación

```json
{
  "token": "jwt_generado_por_el_sistema",
  "tipo": "Bearer",
  "usuario": {
    "id": 1,
    "nombre": "Administrador",
    "correo": "admin@tambo.com",
    "perfil": "Administrador"
  }
}
```

#### Ejemplo de Response de producto

```json
{
  "id": 1,
  "nombre": "Gaseosa personal",
  "sku": "PROD-001",
  "stock": 100,
  "stockMinimo": 20,
  "categoria": "Bebidas",
  "almacen": "Almacén Central",
  "estado": "ACTIVO"
}
```

#### Ejemplo de Response de pedido interno

```json
{
  "id": 10,
  "tienda": "Tienda Chiclayo Centro",
  "fechaSolicitud": "2025-11-15",
  "prioridad": "Alta",
  "estado": "APROBADO",
  "solicitadoPor": "Encargado de tienda",
  "items": [
    {
      "producto": "Gaseosa personal",
      "cantidad": 50
    }
  ]
}
```

#### Ejemplo de Response de transferencia

```json
{
  "id": 5,
  "almacenOrigen": "Almacén Central",
  "almacenDestino": "Tienda Chiclayo Centro",
  "estado": "REGISTRADA",
  "responsable": "Encargado de almacén",
  "detalle": [
    {
      "producto": "Gaseosa personal",
      "cantidad": 50
    }
  ]
}
```

#### Ejemplo de Response de ruta de entrega

```json
{
  "id": 3,
  "nombre": "Ruta Chiclayo Centro",
  "vehiculo": "Camión",
  "placa": "ABC-123",
  "repartidor": "Juan Pérez",
  "origen": "Almacén Central",
  "destino": "Tienda Chiclayo Centro",
  "estado": "ENTREGADO",
  "confirmacionEntrega": true
}
```

---

### 8.4.3 Importancia de los Request y Response

El uso de DTO `Request` y `Response` permite:

- Separar la información enviada por el frontend de las entidades internas del backend.
- Proteger datos sensibles del sistema.
- Validar mejor los datos recibidos.
- Ordenar la comunicación entre frontend y backend.
- Facilitar el mantenimiento del sistema.
- Evitar errores al modificar directamente las entidades de la base de datos.

En este sistema, los `Request` son utilizados principalmente para registrar y actualizar información, mientras que los `Response` se utilizan para devolver resultados procesados al frontend.


### 8.5 Paquete `model`

Contiene las entidades principales del sistema, relacionadas con las tablas de la base de datos.

Modelos principales:

- `Almacen.java`
- `Categoria.java`
- `Cliente.java`
- `DetalleVenta.java`
- `Modulo.java`
- `MovimientoInventario.java`
- `PedidoInterno.java`
- `PedidoInternoItem.java`
- `Perfil.java`
- `Producto.java`
- `Proveedor.java`
- `ProveedorEntrega.java`
- `RutaEntrega.java`
- `Transferencia.java`
- `TransferenciaDetalle.java`
- `User.java`
- `Venta.java`


### 8.6 Paquete `repository`

El paquete `repository` contiene las interfaces encargadas de la comunicación directa con la base de datos. En un proyecto Spring Boot, estas interfaces suelen extender de `JpaRepository`, lo que permite realizar operaciones CRUD sin necesidad de escribir consultas SQL manuales para las operaciones básicas.

Los repositorios se encargan de consultar, registrar, actualizar y eliminar información de las tablas del sistema.

#### Responsabilidades principales del paquete `repository`

- Conectar las entidades del sistema con la base de datos.
- Realizar operaciones CRUD.
- Buscar registros por identificador.
- Consultar información por estado, nombre, categoría, almacén u otros campos.
- Apoyar a la capa de servicios en el acceso a datos.

#### Repositorios principales recomendados

| Repository | Entidad relacionada | Descripción |
|---|---|---|
| `AlmacenRepository` | `Almacen` | Gestiona consultas de almacenes y tiendas. |
| `CategoriaRepository` | `Categoria` | Gestiona consultas de categorías. |
| `ProductoRepository` | `Producto` | Gestiona consultas de productos e inventario. |
| `ProveedorRepository` | `Proveedor` | Gestiona consultas de proveedores. |
| `ProveedorEntregaRepository` | `ProveedorEntrega` | Gestiona entregas realizadas por proveedores. |
| `MovimientoInventarioRepository` | `MovimientoInventario` | Gestiona movimientos de entrada y salida de productos. |
| `PedidoInternoRepository` | `PedidoInterno` | Gestiona pedidos internos de reposición. |
| `PedidoInternoItemRepository` | `PedidoInternoItem` | Gestiona los productos solicitados en cada pedido interno. |
| `TransferenciaRepository` | `Transferencia` | Gestiona transferencias entre almacén central y tiendas. |
| `TransferenciaDetalleRepository` | `TransferenciaDetalle` | Gestiona el detalle de productos transferidos. |
| `RutaEntregaRepository` | `RutaEntrega` | Gestiona rutas de entrega y distribución. |
| `UserRepository` | `User` | Gestiona usuarios del sistema. |
| `PerfilRepository` | `Perfil` | Gestiona perfiles de usuario. |
| `ModuloRepository` | `Modulo` | Gestiona módulos del sistema. |

#### Ejemplo de Repository

```java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEstado(String estado);

    List<Producto> findByStockLessThanEqual(Integer stockMinimo);
}
```

---

### 8.7 Paquete `service`

El paquete `service` contiene la lógica de negocio del sistema. Esta capa actúa como intermediaria entre los controladores y los repositorios.

Los controladores reciben las solicitudes del frontend, pero no deberían acceder directamente a la base de datos. Para mantener una arquitectura ordenada, el controlador llama al servicio, y el servicio utiliza el repositorio correspondiente.

#### Responsabilidades principales del paquete `service`

- Aplicar reglas de negocio.
- Validar datos antes de registrar o actualizar información.
- Coordinar operaciones entre varias entidades.
- Llamar a los repositorios para acceder a la base de datos.
- Controlar estados de pedidos, transferencias y rutas.
- Registrar movimientos de inventario.
- Preparar respuestas para el frontend.

#### Servicios principales recomendados

| Service | Descripción |
|---|---|
| `AuthService` | Gestiona autenticación, login, logout y renovación de token. |
| `AlmacenService` | Gestiona almacenes y tiendas. |
| `CategoriaService` | Gestiona categorías de productos. |
| `ProductoService` | Gestiona productos, stock y validaciones de inventario. |
| `ProveedorService` | Gestiona proveedores. |
| `ProveedorEntregaService` | Gestiona entregas de proveedores al almacén central. |
| `InventarioService` | Gestiona movimientos de inventario. |
| `PedidoInternoService` | Gestiona pedidos internos, aprobación, rechazo y cambio de estados. |
| `TransferenciaService` | Gestiona transferencias del almacén central hacia tiendas. |
| `RutaEntregaService` | Gestiona rutas de entrega, incidencias y confirmación de entregas. |
| `UserService` | Gestiona usuarios del sistema. |
| `PerfilService` | Gestiona perfiles y permisos. |
| `ModuloService` | Gestiona módulos del sistema. |

#### Ejemplo de Service

```java
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto registrarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }
}
```

---

### 8.8 Flujo entre Controller, Service y Repository

El backend utiliza una estructura por capas para mantener el sistema ordenado y fácil de mantener.

```text
Frontend Angular
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Base de datos MySQL
```

#### Descripción del flujo

1. El usuario realiza una acción desde el frontend.
2. El frontend envía un `Request` al backend.
3. El `Controller` recibe la solicitud.
4. El `Service` procesa la lógica de negocio.
5. El `Repository` consulta o modifica la base de datos.
6. El backend devuelve un `Response` al frontend.

#### Ejemplo aplicado al módulo de pedidos internos

```text
Encargado de tienda registra pedido interno
  ↓
PedidoInternoController recibe el Request
  ↓
PedidoInternoService valida la solicitud
  ↓
PedidoInternoRepository guarda el pedido
  ↓
El sistema devuelve un Response con el estado del pedido
```

#### Ejemplo aplicado al módulo de transferencias

```text
Administrador aprueba pedido interno
  ↓
TransferenciaController recibe la solicitud
  ↓
TransferenciaService valida stock y genera transferencia
  ↓
ProductoRepository actualiza stock
  ↓
TransferenciaRepository registra la transferencia
  ↓
MovimientoInventarioRepository registra la salida
  ↓
El sistema devuelve un Response de transferencia registrada
```


---

## 9. Estructura del frontend

El frontend se encuentra en la carpeta:

```text
sistematambo/frontend
```

### 9.1 Carpeta `core`

Contiene elementos centrales del sistema, como autenticación, guards, interceptores y servicios.

Subcarpetas principales:

- `auth`: contiene guardas e interceptores de autenticación.
- `services`: contiene los servicios que consumen los endpoints del backend.

Servicios principales:

- `auth.ts`
- `categories.ts`
- `clientes.ts`
- `employees.ts`
- `internal-orders.ts`
- `inventory.ts`
- `products.ts`
- `profiles.ts`
- `routes.ts`
- `sales.ts`

### 9.2 Carpeta `features`

Contiene las páginas principales del sistema.

Páginas principales:

- `asistencia-page`
- `clientes-page`
- `dashboard-home`
- `categories-page`
- `internal-orders-page`
- `inventory-page`
- `products-page`
- `reports-page`
- `sales-page`
- `stores-page`
- `suppliers-page`
- `transfers-page`
- `warehouses-page`
- `perfiles-page`
- `usuarios-page`

### 9.3 Carpeta `layout`

Contiene la estructura visual principal del sistema, como el menú lateral.

Archivo principal:

- `sidebar-layout`

### 9.4 Carpeta `shared`

Contiene componentes reutilizables del sistema.

Ejemplo:

- `app-icon`

### 9.5 Carpeta `assets`

Contiene imágenes, modelos y recursos estáticos utilizados por el frontend.

---

## 10. Módulos del sistema

## 10.1 Módulo de proveedores

Permite registrar, editar, consultar y gestionar proveedores que abastecen productos al almacén central.

### Funciones principales

- Registrar proveedor.
- Editar información del proveedor.
- Consultar proveedores activos.
- Registrar entregas realizadas por proveedores.
- Asociar productos suministrados por proveedor.

### Entidades relacionadas

- `Proveedor`
- `ProveedorEntrega`
- `Producto`
- `Almacen`

---

## 10.2 Módulo de productos

Permite administrar los productos del sistema logístico.

### Funciones principales

- Registrar productos.
- Editar datos del producto.
- Consultar stock.
- Asociar producto a una categoría.
- Controlar stock mínimo.
- Activar o desactivar productos.

### Entidades relacionadas

- `Producto`
- `Categoria`
- `Almacen`
- `MovimientoInventario`

---

## 10.3 Módulo de categorías

Permite clasificar los productos para facilitar su gestión.

### Funciones principales

- Registrar categoría.
- Editar categoría.
- Consultar categorías.
- Asociar productos a categorías.

### Entidades relacionadas

- `Categoria`
- `Producto`

---

## 10.4 Módulo de almacenes y tiendas

Permite administrar el almacén central y las tiendas destino.

### Funciones principales

- Registrar almacenes.
- Registrar tiendas.
- Consultar capacidad.
- Consultar ocupación.
- Controlar estado del almacén o tienda.
- Asociar productos a un almacén o tienda.

### Entidades relacionadas

- `Almacen`
- `Producto`
- `Transferencia`
- `PedidoInterno`

---

## 10.5 Módulo de inventario

Permite controlar las existencias de productos en almacenes y tiendas.

### Funciones principales

- Consultar stock actual.
- Registrar movimientos de entrada.
- Registrar movimientos de salida.
- Controlar stock mínimo.
- Consultar historial de movimientos.
- Actualizar inventario después de entregas y transferencias.

### Entidades relacionadas

- `MovimientoInventario`
- `Producto`
- `Almacen`

---

## 10.6 Módulo de pedidos internos

Permite que las tiendas soliciten productos al almacén central cuando el stock sea bajo.

### Funciones principales

- Registrar pedido interno.
- Seleccionar tienda solicitante.
- Seleccionar productos y cantidades.
- Definir prioridad.
- Revisar solicitud.
- Aprobar, rechazar o dejar pendiente el pedido.
- Generar transferencia a partir del pedido aprobado.

### Entidades relacionadas

- `PedidoInterno`
- `PedidoInternoItem`
- `Producto`
- `Almacen`
- `Transferencia`

---

## 10.7 Módulo de transferencias

Permite registrar el traslado de productos desde el almacén central hacia una tienda.

### Funciones principales

- Crear transferencia.
- Seleccionar almacén origen.
- Seleccionar tienda destino.
- Agregar productos y cantidades.
- Validar stock disponible.
- Registrar transferencia.
- Descontar stock del almacén central.
- Actualizar stock de tienda al confirmar recepción.

### Entidades relacionadas

- `Transferencia`
- `TransferenciaDetalle`
- `Producto`
- `Almacen`
- `MovimientoInventario`

---

## 10.8 Módulo de rutas de entrega

Permite gestionar la distribución física de productos hacia las tiendas.

### Funciones principales

- Crear ruta de entrega.
- Asignar vehículo.
- Registrar placa.
- Asignar repartidor.
- Definir origen y destino.
- Registrar incidencias.
- Confirmar entrega.
- Cambiar estado de ruta a entregado.

### Entidades relacionadas

- `RutaEntrega`
- `Transferencia`
- `PedidoInterno`

---

## 10.9 Módulo de usuarios y perfiles

Permite gestionar los accesos al sistema.

### Funciones principales

- Registrar usuarios.
- Editar usuarios.
- Activar o desactivar usuarios.
- Asignar perfiles.
- Configurar permisos por módulo.
- Controlar acceso según rol.

### Entidades relacionadas

- `User`
- `Perfil`
- `Modulo`

---

## 11. Roles del sistema

| Rol | Descripción |
|---|---|
| Administrador | Tiene acceso completo al sistema. Gestiona usuarios, perfiles, productos, proveedores, almacenes, pedidos, transferencias y rutas. |
| Encargado de almacén central | Revisa pedidos internos, prepara productos, controla stock y genera transferencias. |
| Encargado de tienda | Revisa stock de tienda, registra pedidos internos y confirma recepción de productos. |
| Transportista | Realiza el traslado de productos y reporta incidencias durante la ruta. |

---

## 12. Procesos principales del sistema

## 12.1 BPMN 1: Proceso de abastecimiento del proveedor al almacén central

Este proceso inicia cuando el proveedor prepara y entrega productos al almacén central. El encargado recibe la mercadería y verifica la cantidad y estado de los productos.

Si la entrega es conforme, el sistema registra la entrega, actualiza el stock del almacén central y genera un movimiento de inventario. Si la entrega no es conforme, se registra la observación o rechazo.

### Flujo resumido

```text
Inicio
Proveedor prepara productos
Proveedor entrega productos al almacén central
Encargado recibe mercadería
Encargado verifica cantidad y estado
¿Entrega conforme?
No → Registrar observación / rechazo → Fin
Sí → Registrar entrega en sistema
Sistema actualiza stock del almacén central
Sistema registra movimiento de inventario
Fin
```

---

## 12.2 BPMN 2: Proceso de pedido interno de reposición de tienda al almacén central

Este proceso inicia cuando el encargado de tienda revisa el stock disponible. Si el stock está bajo, registra un pedido interno de reposición indicando tienda, producto, cantidad y prioridad.

El sistema guarda el pedido y el administrador revisa la solicitud. Si existe stock suficiente en el almacén central, se aprueba el pedido y se prepara la transferencia. En caso contrario, el pedido se rechaza o queda pendiente.

### Flujo resumido

```text
Inicio
Encargado de tienda revisa stock disponible
¿El stock está bajo?
No → Mantener inventario actual → Fin
Sí → Registrar pedido interno de reposición
Seleccionar tienda, producto, cantidad y prioridad
Sistema guarda el pedido interno
Administrador revisa solicitud
¿Hay stock suficiente en almacén central?
No → Rechazar o dejar pendiente el pedido → Notificar a tienda → Fin
Sí → Aprobar pedido interno
Cambiar estado a Aprobado
Preparar productos solicitados
Cambiar estado a En preparación
Generar transferencia hacia tienda
Fin
```

---

## 12.3 BPMN 3: Proceso de transferencia de productos del almacén central hacia tienda

Este proceso inicia cuando un pedido interno ha sido aprobado. El almacén central crea la transferencia y prepara los productos. El sistema valida si existe stock suficiente.

Si el stock es suficiente, se registra la transferencia y se descuenta el stock del almacén central. Luego, la tienda recibe los productos. Si la recepción es conforme, se confirma la recepción y el sistema actualiza el stock de la tienda.

### Flujo resumido

```text
Inicio
Pedido interno aprobado
Crear transferencia
Preparar productos
Validar stock disponible
¿Stock suficiente?
No → Ajustar o cancelar transferencia → Fin
Sí → Registrar transferencia
Descontar stock del almacén central
Recibir productos en tienda
¿Recepción conforme?
No → Registrar incidencia → Fin
Sí → Confirmar recepción
Actualizar stock de tienda
Fin
```

---

## 12.4 BPMN 4: Proceso de distribución y ruta de entrega hacia tienda

Este proceso inicia cuando la transferencia ya fue registrada. El almacén central o el sistema asigna la ruta, vehículo y repartidor. El transportista realiza el traslado de productos hacia la tienda destino.

Si existe una incidencia durante la ruta, se registra la incidencia. Si no hay inconvenientes, el transportista entrega los productos en la tienda. La tienda verifica los productos recibidos y, si la entrega es conforme, se confirma la entrega y el sistema actualiza el estado a entregado.

### Flujo resumido

```text
Inicio
Transferencia registrada
Asignar ruta, vehículo y repartidor
Trasladar productos
¿Hay incidencia?
Sí → Registrar incidencia → Fin
No → Entregar productos en tienda
Verificar productos recibidos
¿Entrega conforme?
No → Registrar observación → Fin
Sí → Confirmar entrega
Actualizar estado a Entregado
Fin
```

---

## 13. Base de datos

El sistema utiliza una base de datos relacional MySQL.

### 13.1 Tablas principales

| Tabla | Descripción |
|---|---|
| `almacenes` | Almacena información del almacén central y tiendas. |
| `productos` | Almacena información de productos, stock, precios y categoría. |
| `categorias` | Clasifica los productos del sistema. |
| `proveedores` | Registra los proveedores que abastecen productos. |
| `proveedores_entregas` | Registra entregas realizadas por proveedores. |
| `movimientos_inventario` | Registra entradas y salidas de productos. |
| `pedidos_internos` | Registra solicitudes de reposición de tiendas. |
| `pedidos_internos_items` | Registra el detalle de productos solicitados en cada pedido. |
| `transferencias` | Registra transferencias desde almacén central hacia tiendas. |
| `transferencias_detalle` | Registra productos y cantidades de cada transferencia. |
| `rutas_entrega` | Registra información de rutas, vehículos, repartidores y entregas. |
| `usuarios` | Registra usuarios del sistema. |
| `perfiles` | Registra roles o perfiles de usuario. |
| `modulos` | Registra módulos disponibles del sistema. |
| `perfil_modulo` | Relaciona perfiles con módulos permitidos. |

---

## 14. Relaciones principales de la base de datos

| Relación | Descripción |
|---|---|
| Categoría 1:N Producto | Una categoría puede tener varios productos. |
| Almacén 1:N Producto | Un almacén o tienda puede tener varios productos. |
| Proveedor 1:N ProveedorEntrega | Un proveedor puede realizar varias entregas. |
| Producto 1:N MovimientoInventario | Un producto puede tener varios movimientos. |
| Almacén 1:N PedidoInterno | Una tienda puede generar varios pedidos internos. |
| PedidoInterno 1:N PedidoInternoItem | Un pedido puede tener varios productos solicitados. |
| Transferencia 1:N TransferenciaDetalle | Una transferencia puede contener varios productos. |
| RutaEntrega N:1 Transferencia | Una ruta puede estar asociada a una transferencia. |
| Perfil 1:N Usuario | Un perfil puede estar asociado a varios usuarios. |
| Perfil N:N Módulo | Un perfil puede tener acceso a varios módulos. |

---

## 15. Seguridad del sistema

El sistema cuenta con autenticación de usuarios y control de acceso por perfiles.

### 15.1 Autenticación

El usuario debe iniciar sesión con credenciales válidas para acceder al sistema.

### 15.2 Autorización

El acceso a los módulos se controla según el perfil del usuario.

### 15.3 Configuración de seguridad

La configuración de seguridad se encuentra en:

```text
backend/src/main/java/com/tambo/sistematambo/config/SecurityConfig.java
```

### 15.4 Interceptor del frontend

El frontend utiliza un interceptor para enviar el token de autenticación en las solicitudes HTTP.

Ubicación:

```text
frontend/src/app/core/auth/auth.interceptor.ts
```

---

### 15.5 Proteccion de reportes

Los endpoints `/api/reportes/**` no son publicos. En `SecurityConfig.java` se exige rol `ADMINISTRADOR` para descargar reportes Excel. Esta regla evita que usuarios anonimos o sin permisos descarguen informacion operativa del sistema.

La proteccion esta respaldada por la prueba:

```text
backend/src/test/java/com/tambo/sistematambo/security/ReporteSecurityIntegrationTest.java
```

### 15.6 Refresh token protegido

El sistema entrega el refresh token al usuario autenticado, pero no lo guarda en texto plano. Antes de persistirlo se calcula un hash SHA-256 mediante `RefreshTokenHashService`. En la renovacion de sesion, el token recibido se vuelve a hashear y se compara contra el valor almacenado.

Esta medida reduce el impacto si alguien accede a la base de datos, porque no encontrara refresh tokens reutilizables directamente.

Pruebas asociadas:

```text
backend/src/test/java/com/tambo/sistematambo/security/RefreshTokenHashServiceTest.java
backend/src/test/java/com/tambo/sistematambo/service/AuthServiceTest.java
```

---

## 16. Servicios REST principales

Los controladores del backend exponen endpoints REST que son consumidos por el frontend.

| Controlador | Función |
|---|---|
| `AuthController` | Gestiona autenticación. |
| `ProductoController` | Gestiona productos. |
| `ProveedorController` | Gestiona proveedores. |
| `AlmacenController` | Gestiona almacenes y tiendas. |
| `InventarioController` | Gestiona inventario y movimientos. |
| `PedidoInternoController` | Gestiona pedidos internos. |
| `RutaEntregaController` | Gestiona rutas de entrega. |
| `PerfilController` | Gestiona perfiles. |
| `ModuloController` | Gestiona módulos. |
| `UserController` | Gestiona usuarios. |


### 16.1 Documentación de contratos Request y Response por módulo

Los contratos `Request` y `Response` definen la estructura de comunicación entre el frontend Angular y el backend Spring Boot.

| Módulo | Request principal | Response principal |
|---|---|---|
| Autenticación | `AuthRequest`, `AuthRefreshRequest`, `AuthLogoutRequest` | `AuthResponse`, `MensajeResponse` |
| Usuarios | Datos de usuario y perfil | `UsuarioResponse` |
| Proveedores | Datos del proveedor | `ProveedorResponse` |
| Productos | `ProductoRequest` | `ProductoResponse` |
| Categorías | `CategoriaRequest` | Datos de categoría registrada |
| Almacenes y tiendas | `AlmacenRequest` | `AlmacenResponse` |
| Inventario | Datos de movimiento de inventario | Datos del movimiento registrado |
| Pedidos internos | `PedidoInternoRequest`, `PedidoInternoItemRequest`, `PedidoInternoEstadoRequest` | `PedidoInternoResponse` |
| Transferencias | Datos de transferencia y detalle | `TransferenciaResponse` |
| Rutas de entrega | Datos de ruta, vehículo y repartidor | `RutaEntregaResponse` |
| Perfiles y permisos | `PerfilPermisosRequest` | Datos del perfil con módulos asignados |

#### Ejemplo de comunicación entre frontend y backend

```text
Frontend Angular
  ↓ envía Request
Backend Spring Boot
  ↓ procesa datos
Base de datos MySQL
  ↓ retorna información
Backend Spring Boot
  ↓ devuelve Response
Frontend Angular
```

#### Ejemplo general de respuesta exitosa

```json
{
  "success": true,
  "message": "Operación realizada correctamente",
  "data": {
    "id": 1,
    "estado": "ACTIVO"
  }
}
```

#### Ejemplo general de respuesta con error

```json
{
  "success": false,
  "message": "No se pudo procesar la solicitud",
  "error": "Datos incompletos"
}
```


---

## 17. Configuración con Docker

El sistema utiliza Docker para levantar los servicios principales mediante contenedores.

### 17.1 Servicios esperados

| Servicio | Descripción | Puerto sugerido |
|---|---|---|
| Backend | API REST Spring Boot | 8080 |
| Frontend | Aplicación Angular | 4200 |
| MySQL | Base de datos | 3306 |

### 17.2 Requisitos previos

Antes de ejecutar el sistema se debe instalar:

- Docker
- Docker Compose
- Git
- Visual Studio Code

### 17.3 Levantar el sistema con Docker Compose

Desde la raíz del proyecto, ejecutar:

```bash
docker compose up --build
```

Este comando construye las imágenes y levanta los contenedores definidos en el archivo `docker-compose.yml`.

### 17.4 Detener los contenedores

```bash
docker compose down
```

### 17.5 Ver contenedores activos

```bash
docker ps
```

### 17.6 Ver logs del sistema

```bash
docker compose logs
```

Logs del backend:

```bash
docker compose logs backend
```

Logs del frontend:

```bash
docker compose logs frontend
```

Logs de MySQL:

```bash
docker compose logs mysql
```

---

## 18. Instalación del sistema

### 18.1 Clonar el proyecto

```bash
git clone <url-del-repositorio>
```

### 18.2 Ingresar a la carpeta del proyecto

```bash
cd sistematambo
```

### 18.3 Levantar los servicios

```bash
docker compose up --build
```

### 18.4 Acceder al sistema

Abrir el navegador e ingresar a:

```text
http://localhost:4200
```

### 18.5 Acceder al backend

El backend estará disponible en:

```text
http://localhost:8080
```

---

## 19. Ejecución manual del backend

Si se desea ejecutar el backend sin Docker:

```bash
cd backend
mvn spring-boot:run
```

El backend se ejecutará en:

```text
http://localhost:8080
```

---

## 20. Ejecución manual del frontend

Si se desea ejecutar el frontend sin Docker:

```bash
cd frontend
npm install
ng serve
```

El frontend se ejecutará en:

```text
http://localhost:4200
```

---

## 21. Variables de entorno

Las variables de entorno permiten configurar la conexión entre servicios.

### 21.1 Backend

Ejemplo de variables utilizadas por el backend:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/sistematambo
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456
JWT_SECRET=clave_secreta
```

### 21.2 Frontend

El frontend configura la URL del backend en los archivos de entorno:

```text
frontend/src/environments/environment.ts
frontend/src/environments/environment.development.ts
```

Ejemplo:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

---

## 22. Requerimientos funcionales

| Código | Requerimiento funcional |
|---|---|
| RF01 | El sistema debe permitir iniciar sesión con usuario y contraseña. |
| RF02 | El sistema debe permitir gestionar usuarios. |
| RF03 | El sistema debe permitir gestionar perfiles y permisos. |
| RF04 | El sistema debe permitir registrar proveedores. |
| RF05 | El sistema debe permitir registrar productos. |
| RF06 | El sistema debe permitir registrar categorías. |
| RF07 | El sistema debe permitir gestionar almacenes y tiendas. |
| RF08 | El sistema debe permitir consultar inventario. |
| RF09 | El sistema debe permitir registrar movimientos de inventario. |
| RF10 | El sistema debe permitir registrar entregas de proveedores. |
| RF11 | El sistema debe permitir registrar pedidos internos de reposición. |
| RF12 | El sistema debe permitir aprobar, rechazar o dejar pendiente un pedido interno. |
| RF13 | El sistema debe permitir generar transferencias hacia tiendas. |
| RF14 | El sistema debe permitir descontar stock del almacén central. |
| RF15 | El sistema debe permitir actualizar stock de tienda. |
| RF16 | El sistema debe permitir registrar rutas de entrega. |
| RF17 | El sistema debe permitir registrar incidencias de distribución. |
| RF18 | El sistema debe permitir confirmar entregas. |
| RF19 | El sistema debe permitir consultar reportes logísticos. |

---

## 23. Requerimientos no funcionales

| Código | Requerimiento no funcional |
|---|---|
| RNF01 | El sistema debe ser accesible desde un navegador web. |
| RNF02 | El sistema debe contar con autenticación segura. |
| RNF03 | El sistema debe restringir el acceso según el perfil del usuario. |
| RNF04 | El sistema debe mantener la integridad de la información. |
| RNF05 | El sistema debe permitir consultas rápidas de inventario. |
| RNF06 | El sistema debe ejecutarse mediante contenedores Docker. |
| RNF07 | El sistema debe tener una interfaz clara y fácil de usar. |
| RNF08 | El sistema debe permitir mantenimiento y escalabilidad. |
| RNF09 | El sistema debe separar frontend y backend para facilitar su desarrollo. |
| RNF10 | El sistema debe permitir futuras integraciones con otros sistemas. |

---

## 24. Uso general del sistema

1. El usuario accede al sistema desde el navegador.
2. El usuario inicia sesión con sus credenciales.
3. El sistema valida el usuario y perfil.
4. El usuario accede a los módulos permitidos.
5. El encargado registra productos, proveedores, almacenes o pedidos.
6. El almacén central revisa pedidos internos.
7. El sistema genera transferencias cuando corresponde.
8. El transportista realiza la distribución.
9. La tienda confirma la recepción de productos.
10. El sistema actualiza el inventario y estados.

---

## 25. Reportes del sistema

El sistema cuenta con una sección de reportes para apoyar la toma de decisiones logísticas.

Reportes sugeridos:

- Reporte de productos con bajo stock.
- Reporte de movimientos de inventario.
- Reporte de pedidos internos por estado.
- Reporte de transferencias realizadas.
- Reporte de entregas de proveedores.
- Reporte de rutas de entrega.
- Reporte de productos por almacén o tienda.

---

## 26. Mantenimiento del sistema

Para mantener correctamente el sistema se recomienda:

- Revisar periódicamente los logs del backend.
- Verificar el estado de los contenedores Docker.
- Realizar respaldos de la base de datos.
- Actualizar dependencias del frontend y backend.
- Validar la seguridad de usuarios y permisos.
- Revisar errores reportados por los usuarios.
- Mantener actualizada la documentación técnica.

---

## 27. Posibles mejoras futuras

- Implementar alertas automáticas de stock mínimo.
- Implementar alertas de productos próximos a vencer.
- Agregar seguimiento GPS en tiempo real.
- Generar reportes exportables en PDF o Excel.
- Implementar notificaciones automáticas.
- Mejorar el panel de indicadores logísticos.
- Integrar el sistema con lectores de códigos de barras.
- Implementar auditoría de cambios por usuario.

---

## 28. Conclusión técnica

El Sistema Logístico Tambo permite automatizar procesos clave de abastecimiento, inventario, pedidos internos, transferencias y distribución. Su arquitectura separa el backend en Java Spring Boot y el frontend en Angular, facilitando el mantenimiento y escalabilidad del sistema.

El uso de Docker permite ejecutar los servicios de forma ordenada y reproducible. Asimismo, la documentación técnica en Markdown permite que otros desarrolladores comprendan la estructura, tecnologías, procesos, módulos y forma de instalación del sistema.
