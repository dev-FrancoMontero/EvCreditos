-- =====================================================================
-- Sistema Evaluador de Créditos - Stored Procedures
-- Toda la aplicación accede a la base solo a través de estos procedimientos.
--
-- Errores de validación: se lanzan con SIGNAL SQLSTATE '45000' y un mensaje
-- en castellano. En Java llegan como SQLException; e.getMessage() devuelve
-- ese mensaje, listo para mostrar en un JOptionPane.
-- =====================================================================

USE Creditos;

DROP PROCEDURE IF EXISTS sp_validar_datos_cliente;
DROP PROCEDURE IF EXISTS sp_guardar_persona_fisica;
DROP PROCEDURE IF EXISTS sp_guardar_empresa;
DROP PROCEDURE IF EXISTS sp_listar_clientes;
DROP PROCEDURE IF EXISTS sp_eliminar_cliente;
DROP PROCEDURE IF EXISTS sp_validar_login;
DROP PROCEDURE IF EXISTS sp_crear_usuario;
DROP PROCEDURE IF EXISTS sp_listar_usuarios;
DROP PROCEDURE IF EXISTS sp_eliminar_usuario;

DELIMITER $$

-- ---------------------------------------------------------------------
-- CLIENTES
-- ---------------------------------------------------------------------

-- Validaciones comunes a todo tipo de cliente (uso interno).
CREATE PROCEDURE sp_validar_datos_cliente(
    IN p_nombre   VARCHAR(255),
    IN p_ingresos DECIMAL(12, 2),
    IN p_gastos   DECIMAL(12, 2)
)
BEGIN
    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre del cliente es obligatorio.';
    END IF;
    IF CHAR_LENGTH(TRIM(p_nombre)) > 30 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre del cliente no puede superar los 30 caracteres.';
    END IF;
    IF p_ingresos IS NULL OR p_ingresos < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Los ingresos deben ser un número mayor o igual a 0.';
    END IF;
    IF p_gastos IS NULL OR p_gastos < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Los gastos deben ser un número mayor o igual a 0.';
    END IF;
END$$

-- Guarda un cliente persona física (tabla padre + tabla hija) en una
-- sola transacción. Devuelve el id_cliente generado.
CREATE PROCEDURE sp_guardar_persona_fisica(
    IN  p_nombre     VARCHAR(255),
    IN  p_ingresos   DECIMAL(12, 2),
    IN  p_gastos     DECIMAL(12, 2),
    IN  p_historial  BOOLEAN,
    OUT p_id_cliente INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    CALL sp_validar_datos_cliente(p_nombre, p_ingresos, p_gastos);

    START TRANSACTION;
    INSERT INTO clientes (nombre, ingresos, gastos, historial)
    VALUES (TRIM(p_nombre), p_ingresos, p_gastos, IFNULL(p_historial, FALSE));
    SET p_id_cliente = LAST_INSERT_ID();

    INSERT INTO personas_fisicas (id_cliente) VALUES (p_id_cliente);
    COMMIT;
END$$

-- Guarda un cliente empresa (tabla padre + tabla hija) en una sola
-- transacción. Devuelve el id_cliente generado.
CREATE PROCEDURE sp_guardar_empresa(
    IN  p_nombre     VARCHAR(255),
    IN  p_ingresos   DECIMAL(12, 2),
    IN  p_gastos     DECIMAL(12, 2),
    IN  p_historial  BOOLEAN,
    IN  p_rubro      VARCHAR(255),
    IN  p_director   VARCHAR(255),
    OUT p_id_cliente INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    CALL sp_validar_datos_cliente(p_nombre, p_ingresos, p_gastos);

    IF p_rubro IS NULL OR TRIM(p_rubro) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rubro de la empresa es obligatorio.';
    END IF;
    IF CHAR_LENGTH(TRIM(p_rubro)) > 30 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rubro no puede superar los 30 caracteres.';
    END IF;
    IF p_director IS NULL OR TRIM(p_director) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El director de la empresa es obligatorio.';
    END IF;
    IF CHAR_LENGTH(TRIM(p_director)) > 30 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El director no puede superar los 30 caracteres.';
    END IF;

    START TRANSACTION;
    INSERT INTO clientes (nombre, ingresos, gastos, historial)
    VALUES (TRIM(p_nombre), p_ingresos, p_gastos, IFNULL(p_historial, FALSE));
    SET p_id_cliente = LAST_INSERT_ID();

    INSERT INTO empresas (id_cliente, rubro, director)
    VALUES (p_id_cliente, TRIM(p_rubro), TRIM(p_director));
    COMMIT;
END$$

-- Lista todos los clientes con su tipo ('PERSONA_FISICA' o 'EMPRESA').
-- rubro y director vienen en NULL para las personas físicas.
CREATE PROCEDURE sp_listar_clientes()
BEGIN
    SELECT c.id_cliente,
           c.nombre,
           c.ingresos,
           c.gastos,
           c.historial,
           CASE WHEN e.id IS NOT NULL THEN 'EMPRESA' ELSE 'PERSONA_FISICA' END AS tipo,
           e.rubro,
           e.director
    FROM clientes c
    LEFT JOIN empresas e ON e.id_cliente = c.id_cliente
    ORDER BY c.id_cliente;
END$$

-- Elimina un cliente (la fila hija se borra sola por ON DELETE CASCADE).
-- Devuelve la cantidad de filas eliminadas: 0 si el id no existía.
CREATE PROCEDURE sp_eliminar_cliente(
    IN  p_id_cliente INT,
    OUT p_filas      INT
)
BEGIN
    DELETE FROM clientes WHERE id_cliente = p_id_cliente;
    SET p_filas = ROW_COUNT();
END$$

-- ---------------------------------------------------------------------
-- USUARIOS
-- ---------------------------------------------------------------------

-- Devuelve TRUE solo si usuario Y clave coinciden exactamente.
CREATE PROCEDURE sp_validar_login(
    IN  p_usr    VARCHAR(255),
    IN  p_pwd    VARCHAR(255),
    OUT p_valido BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_valido
    FROM usuarios
    WHERE usr = p_usr AND pwd = p_pwd;
END$$

-- Crea un usuario. Falla con mensaje claro si los datos están vacíos o
-- si el nombre ya existe. Devuelve el id_usuario generado.
CREATE PROCEDURE sp_crear_usuario(
    IN  p_usr        VARCHAR(255),
    IN  p_pwd        VARCHAR(255),
    OUT p_id_usuario INT
)
BEGIN
    IF p_usr IS NULL OR TRIM(p_usr) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre de usuario es obligatorio.';
    END IF;
    IF CHAR_LENGTH(TRIM(p_usr)) > 30 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre de usuario no puede superar los 30 caracteres.';
    END IF;
    IF p_pwd IS NULL OR p_pwd = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La clave es obligatoria.';
    END IF;
    IF CHAR_LENGTH(p_pwd) > 30 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La clave no puede superar los 30 caracteres.';
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE usr = TRIM(p_usr)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre de usuario ya existe.';
    END IF;

    INSERT INTO usuarios (usr, pwd) VALUES (TRIM(p_usr), p_pwd);
    SET p_id_usuario = LAST_INSERT_ID();
END$$

-- Lista los usuarios sin exponer la clave.
CREATE PROCEDURE sp_listar_usuarios()
BEGIN
    SELECT id_usuario, usr
    FROM usuarios
    ORDER BY id_usuario;
END$$

-- Elimina un usuario por su id. Devuelve la cantidad de filas eliminadas:
-- 0 si el id no existe (no es un error). No permite borrar el último
-- usuario, para que siempre se pueda volver a entrar al sistema.
CREATE PROCEDURE sp_eliminar_usuario(
    IN  p_id_usuario INT,
    OUT p_filas      INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = p_id_usuario) THEN
        SET p_filas = 0;
    ELSEIF (SELECT COUNT(*) FROM usuarios) = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede eliminar el último usuario del sistema.';
    ELSE
        DELETE FROM usuarios WHERE id_usuario = p_id_usuario;
        SET p_filas = ROW_COUNT();
    END IF;
END$$

DELIMITER ;
