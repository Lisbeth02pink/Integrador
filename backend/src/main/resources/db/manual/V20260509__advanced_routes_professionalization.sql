USE sistematambo;

ALTER TABLE rutas_entrega
    ADD COLUMN IF NOT EXISTS transferencia_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS tipo_vehiculo VARCHAR(30) NULL,
    ADD COLUMN IF NOT EXISTS capacidad_vehiculo INT NULL,
    ADD COLUMN IF NOT EXISTS cantidad_carga INT NULL,
    ADD COLUMN IF NOT EXISTS origen VARCHAR(160) NULL,
    ADD COLUMN IF NOT EXISTS destino VARCHAR(160) NULL,
    ADD COLUMN IF NOT EXISTS hora_estimada_llegada VARCHAR(5) NULL,
    ADD COLUMN IF NOT EXISTS hora_entrega_real VARCHAR(5) NULL,
    ADD COLUMN IF NOT EXISTS ubicacion_actual VARCHAR(180) NULL,
    ADD COLUMN IF NOT EXISTS observaciones VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS incidencias VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS estado_gps VARCHAR(30) NULL,
    ADD COLUMN IF NOT EXISTS evidencia_entrega VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS firma_digital VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS foto_entrega VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS vehiculo_activo BIT NOT NULL DEFAULT b'1',
    ADD COLUMN IF NOT EXISTS conductor_bloqueado BIT NOT NULL DEFAULT b'0',
    ADD COLUMN IF NOT EXISTS confirmacion_entrega BIT NOT NULL DEFAULT b'0';

ALTER TABLE rutas_entrega
    ADD CONSTRAINT fk_rutas_entrega_transferencia
        FOREIGN KEY (transferencia_id) REFERENCES transferencias(id);
