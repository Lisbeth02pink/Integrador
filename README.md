# Sistema Logístico Tambo

![Badge en desarrollo](https://img.shields.io/badge/STATUS-EN%20DESARROLLO-green)

## Índice

- [Descripción del proyecto](#descripción-del-proyecto)
- [Estado del proyecto](#estado-del-proyecto)
- [Funcionalidades](#funcionalidades)
- [Acceso al proyecto](#acceso-al-proyecto)
- [Abre y ejecuta el proyecto](#abre-y-ejecuta-el-proyecto)
- [Tecnologías utilizadas](#tecnologías-utilizadas)

## Descripción del proyecto

El Sistema Logístico Tambo es una aplicación web desarrollada para mejorar el proceso de distribución de productos.

El problema principal identificado es que el modelo de entrega directa **Proveedor → Tienda** puede generar desorden, quiebres de stock, exceso de inventario e incongruencias en los registros.

Por ello, se propone una solución basada en el modelo:

**Proveedor → Almacén central → Tienda**

De esta manera, los productos primero llegan al almacén central, donde son recibidos, verificados y registrados. Luego, se distribuyen hacia las tiendas según sus necesidades de reposición.

## Estado del proyecto

🚧 Proyecto en desarrollo 🚧

## Funcionalidades

- Registro y gestión de proveedores.
- Registro y gestión de productos.
- Control de categorías.
- Gestión de almacenes y tiendas.
- Control de inventario.
- Registro de pedidos internos.
- Gestión de transferencias.
- Control de rutas de entrega.
- Gestión de usuarios y perfiles.
- Consulta de reportes logísticos.

## Acceso al proyecto

Para acceder al proyecto, clonar el repositorio con el siguiente comando:

```bash
git clone URL_DEL_REPOSITORIO

## Tecnologías utilizadas

| Tecnología | Uso en el proyecto |
|---|---|
| Spring Boot | Framework utilizado para desarrollar la API REST del sistema. |
| Angular | Framework utilizado para desarrollar el frontend. |
| MySQL | Base de datos relacional del sistema. |
| Docker Compose | Ejecución conjunta del backend, frontend y base de datos. |
