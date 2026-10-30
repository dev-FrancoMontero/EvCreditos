-- =====================================================================
-- Sistema Evaluador de Créditos - Creación de la base de datos y tablas
-- Ejecutar primero este script y después 02_stored_procedures.sql
--
-- Cambios respecto del script original:
--   * clientes: se agregó la columna "gastos" (la evaluación la necesita),
--     NOT NULL en todos los campos y DECIMAL en lugar de FLOAT para montos
--     (FLOAT redondea y no conviene para dinero).
--   * personas_fisicas / empresas: id_cliente NOT NULL y UNIQUE (un cliente
--     tiene un solo subtipo) y ON DELETE CASCADE (si se borra el cliente se
--     borra su fila hija).
--   * usuarios: se agregó id_usuario autoincremental como clave primaria,
--     porque la guía pide eliminar usuarios por su identificador. La columna
--     del nombre se llama "nombre_usuario" y es UNIQUE. Las columnas usan collation binaria
--     para que el login distinga mayúsculas de minúsculas.
--
-- ATENCIÓN: borra y vuelve a crear las tablas (se pierden los datos).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS evaluador_creditos;
USE evaluador_creditos;

DROP TABLE IF EXISTS personas_fisicas;
DROP TABLE IF EXISTS empresas;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(30)    NOT NULL,
    ingresos   DECIMAL(12, 2) NOT NULL,
    gastos     DECIMAL(12, 2) NOT NULL,
    historial  BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_clientes_ingresos CHECK (ingresos >= 0),
    CONSTRAINT chk_clientes_gastos   CHECK (gastos >= 0)
);

CREATE TABLE personas_fisicas (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL UNIQUE,
    FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente) ON DELETE CASCADE
);

CREATE TABLE empresas (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT         NOT NULL UNIQUE,
    rubro      VARCHAR(30) NOT NULL,
    director   VARCHAR(30) NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente) ON DELETE CASCADE
);

CREATE TABLE usuarios (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    pwd            VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    CONSTRAINT uq_usuarios_nombre_usuario UNIQUE (nombre_usuario)
);

-- Usuario inicial para probar el login
INSERT INTO usuarios (nombre_usuario, pwd) VALUES ('Bonillo', 'Pequenio');
