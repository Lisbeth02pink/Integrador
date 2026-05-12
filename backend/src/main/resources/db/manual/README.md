**Migraciones Manuales**

El proyecto usa `spring.jpa.hibernate.ddl-auto=update`, así que estas migraciones no se ejecutan solas.

Ejecuta [V20260505__normalize_sku_by_warehouse.sql](/D:/Sistema_integrador/sistematambo/backend/src/main/resources/db/manual/V20260505__normalize_sku_by_warehouse.sql) en MySQL antes de usar entregas que copien el mismo `SKU` del almacen central hacia tiendas.

La migracion hace esto:

- Quita el indice unico global de `productos.sku` si existe.
- Crea unicidad por `(sku, almacen_id)`.
- Permite que el mismo SKU exista en varias ubicaciones sin duplicarse dentro del mismo almacen.
