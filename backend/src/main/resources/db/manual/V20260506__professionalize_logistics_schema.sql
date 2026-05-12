-- Migracion manual complementaria para profesionalizar el modelo logistico/comercial.
-- Si usas spring.jpa.hibernate.ddl-auto=update, Hibernate creara buena parte de esto automaticamente.

USE sistematambo;

ALTER TABLE almacenes
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(20) NOT NULL DEFAULT 'TIENDA' AFTER direccion;

UPDATE almacenes
SET tipo = CASE
    WHEN LOWER(nombre) LIKE '%central%' THEN 'CENTRAL'
    ELSE 'TIENDA'
END
WHERE tipo IS NULL OR tipo = '';

ALTER TABLE rutas_entrega
    ADD COLUMN IF NOT EXISTS pedido_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS vehiculo VARCHAR(80) NULL,
    ADD COLUMN IF NOT EXISTS placa VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS fecha_entrega DATE NULL;

CREATE TABLE IF NOT EXISTS transferencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    almacen_origen_id BIGINT NOT NULL,
    almacen_destino_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    estado VARCHAR(30) NOT NULL,
    responsable VARCHAR(120) NOT NULL,
    referencia VARCHAR(180) NOT NULL,
    CONSTRAINT fk_transferencias_origen FOREIGN KEY (almacen_origen_id) REFERENCES almacenes(id),
    CONSTRAINT fk_transferencias_destino FOREIGN KEY (almacen_destino_id) REFERENCES almacenes(id)
);

CREATE TABLE IF NOT EXISTS transferencias_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transferencia_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    CONSTRAINT fk_transferencias_detalle_transferencia FOREIGN KEY (transferencia_id) REFERENCES transferencias(id),
    CONSTRAINT fk_transferencias_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE IF NOT EXISTS ventas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NULL,
    fecha DATETIME NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    usuario_id BIGINT NULL,
    almacen_id BIGINT NOT NULL,
    CONSTRAINT fk_ventas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (usuario_id) REFERENCES users(id),
    CONSTRAINT fk_ventas_almacen FOREIGN KEY (almacen_id) REFERENCES almacenes(id)
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_ventas_venta FOREIGN KEY (venta_id) REFERENCES ventas(id),
    CONSTRAINT fk_detalle_ventas_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);
