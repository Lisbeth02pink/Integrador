-- Migracion manual para MySQL 8+
-- Objetivo:
-- 1. Eliminar cualquier indice unico global sobre productos.sku
-- 2. Crear un indice unico compuesto por (sku, almacen_id)
--
-- Ejecuta este script una sola vez sobre la base de datos productiva o de desarrollo
-- antes de usar transferencias que creen el mismo SKU en multiples tiendas.

USE sistematambo;

SET @schema_name = DATABASE();

SELECT COUNT(*) INTO @has_unique_sku
FROM information_schema.statistics
WHERE table_schema = @schema_name
  AND table_name = 'productos'
  AND column_name = 'sku'
  AND non_unique = 0;

SELECT index_name INTO @unique_sku_index
FROM information_schema.statistics
WHERE table_schema = @schema_name
  AND table_name = 'productos'
  AND column_name = 'sku'
  AND non_unique = 0
LIMIT 1;

SET @drop_unique_sku_sql = IF(
  @has_unique_sku > 0,
  CONCAT('ALTER TABLE productos DROP INDEX ', @unique_sku_index),
  'SELECT "No existe indice unico global sobre productos.sku"'
);

PREPARE stmt_drop_unique_sku FROM @drop_unique_sku_sql;
EXECUTE stmt_drop_unique_sku;
DEALLOCATE PREPARE stmt_drop_unique_sku;

SELECT COUNT(*) INTO @has_unique_sku_warehouse
FROM information_schema.statistics
WHERE table_schema = @schema_name
  AND table_name = 'productos'
GROUP BY index_name
HAVING SUM(column_name = 'sku') > 0
   AND SUM(column_name = 'almacen_id') > 0
   AND MIN(non_unique) = 0
LIMIT 1;

SET @create_unique_sku_warehouse_sql = IF(
  IFNULL(@has_unique_sku_warehouse, 0) = 0,
  'ALTER TABLE productos ADD CONSTRAINT uk_productos_sku_almacen UNIQUE (sku, almacen_id)',
  'SELECT "Ya existe un indice unico compuesto sobre (sku, almacen_id)"'
);

PREPARE stmt_create_unique_sku_warehouse FROM @create_unique_sku_warehouse_sql;
EXECUTE stmt_create_unique_sku_warehouse;
DEALLOCATE PREPARE stmt_create_unique_sku_warehouse;

SHOW INDEX FROM productos;
