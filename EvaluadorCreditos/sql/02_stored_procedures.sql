

USE Creditos;

DROP PROCEDURE IF EXISTS sp_validar_datos_cliente;
DROP PROCEDURE IF EXISTS sp_validar_datos_empresa;
DROP PROCEDURE IF EXISTS sp_validar_datos_usuario;
DROP PROCEDURE IF EXISTS sp_listar_clientes;
DROP PROCEDURE IF EXISTS sp_buscar_cliente;
DROP PROCEDURE IF EXISTS sp_eliminar_cliente;
DROP PROCEDURE IF EXISTS sp_alta_persona_fisica;
DROP PROCEDURE IF EXISTS sp_modificar_persona_fisica;
DROP PROCEDURE IF EXISTS sp_listar_personas_fisicas;
DROP PROCEDURE IF EXISTS sp_alta_empresa;
DROP PROCEDURE IF EXISTS sp_modificar_empresa;
DROP PROCEDURE IF EXISTS sp_listar_empresas;
DROP PROCEDURE IF EXISTS sp_validar_login;
DROP PROCEDURE IF EXISTS sp_alta_usuario;
DROP PROCEDURE IF EXISTS sp_modificar_usuario;
DROP PROCEDURE IF EXISTS sp_listar_usuarios;
DROP PROCEDURE IF EXISTS sp_eliminar_usuario;

DELIMITER $$

-- =====================================================================
-- VALIDACIONES (uso interno de los demás procedimientos)
-- =====================================================================

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

CREATE PROCEDURE sp_validar_datos_empresa(
    IN p_rubro    VARCHAR(255),
    IN p_director VARCHAR(255)
)
BEGIN
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
END$$

CREATE PROCEDURE sp_validar_datos_usuario(
    IN p_usr VARCHAR(255),
    IN p_pwd VARCHAR(255)
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
END$$

-- =====================================================================
-- CLIENTES (operaciones comunes a ambos tipos)
-- =====================================================================

-- Lista todos los clientes con su tipo ('PERSONA_FISICA' o 'EMPRESA').
-- rubro y director vienen en NULL para las personas físicas.
CREATE PROCEDURE sp_listar_clientes()
BEGIN
    SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial,
           CASE WHEN e.id IS NOT NULL THEN 'EMPRESA' ELSE 'PERSONA_FISICA' END AS tipo,
           e.rubro, e.director
    FROM clientes c
    LEFT JOIN empresas e ON e.id_cliente = c.id_cliente
    ORDER BY c.id_cliente;
END$$

-- Busca un cliente por id. Devuelve 0 o 1 fila, con las mismas columnas
-- que sp_listar_clientes.
CREATE PROCEDURE sp_buscar_cliente(IN p_id_cliente INT)
BEGIN
    SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial,
           CASE WHEN e.id IS NOT NULL THEN 'EMPRESA' ELSE 'PERSONA_FISICA' END AS tipo,
           e.rubro, e.director
    FROM clientes c
    LEFT JOIN empresas e ON e.id_cliente = c.id_cliente
    WHERE c.id_cliente = p_id_cliente;
END$$

-- Baja de un cliente de cualquier tipo (la fila hija se borra sola por
-- ON DELETE CASCADE).
CREATE PROCEDURE sp_eliminar_cliente(
    IN  p_id_cliente INT,
    OUT p_filas      INT
)
BEGIN
    DELETE FROM clientes WHERE id_cliente = p_id_cliente;
    SET p_filas = ROW_COUNT();
END$$

-- =====================================================================
-- PERSONAS FÍSICAS
-- =====================================================================

-- Alta: inserta en clientes y personas_fisicas en una transacción.
CREATE PROCEDURE sp_alta_persona_fisica(
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

-- Modificación: solo actúa si el id corresponde a una persona física.
CREATE PROCEDURE sp_modificar_persona_fisica(
    IN  p_id_cliente INT,
    IN  p_nombre     VARCHAR(255),
    IN  p_ingresos   DECIMAL(12, 2),
    IN  p_gastos     DECIMAL(12, 2),
    IN  p_historial  BOOLEAN,
    OUT p_filas      INT
)
BEGIN
    CALL sp_validar_datos_cliente(p_nombre, p_ingresos, p_gastos);

    IF NOT EXISTS (SELECT 1 FROM personas_fisicas WHERE id_cliente = p_id_cliente) THEN
        SET p_filas = 0;
    ELSE
        UPDATE clientes
        SET nombre = TRIM(p_nombre), ingresos = p_ingresos,
            gastos = p_gastos, historial = IFNULL(p_historial, FALSE)
        WHERE id_cliente = p_id_cliente;
        SET p_filas = 1;
    END IF;
END$$

CREATE PROCEDURE sp_listar_personas_fisicas()
BEGIN
    SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial
    FROM clientes c
    JOIN personas_fisicas p ON p.id_cliente = c.id_cliente
    ORDER BY c.id_cliente;
END$$

-- =====================================================================
-- EMPRESAS
-- =====================================================================

-- Alta: inserta en clientes y empresas en una transacción.
CREATE PROCEDURE sp_alta_empresa(
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
    CALL sp_validar_datos_empresa(p_rubro, p_director);

    START TRANSACTION;
    INSERT INTO clientes (nombre, ingresos, gastos, historial)
    VALUES (TRIM(p_nombre), p_ingresos, p_gastos, IFNULL(p_historial, FALSE));
    SET p_id_cliente = LAST_INSERT_ID();
    INSERT INTO empresas (id_cliente, rubro, director)
    VALUES (p_id_cliente, TRIM(p_rubro), TRIM(p_director));
    COMMIT;
END$$

-- Modificación: solo actúa si el id corresponde a una empresa.
CREATE PROCEDURE sp_modificar_empresa(
    IN  p_id_cliente INT,
    IN  p_nombre     VARCHAR(255),
    IN  p_ingresos   DECIMAL(12, 2),
    IN  p_gastos     DECIMAL(12, 2),
    IN  p_historial  BOOLEAN,
    IN  p_rubro      VARCHAR(255),
    IN  p_director   VARCHAR(255),
    OUT p_filas      INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    CALL sp_validar_datos_cliente(p_nombre, p_ingresos, p_gastos);
    CALL sp_validar_datos_empresa(p_rubro, p_director);

    IF NOT EXISTS (SELECT 1 FROM empresas WHERE id_cliente = p_id_cliente) THEN
        SET p_filas = 0;
    ELSE
        START TRANSACTION;
        UPDATE clientes
        SET nombre = TRIM(p_nombre), ingresos = p_ingresos,
            gastos = p_gastos, historial = IFNULL(p_historial, FALSE)
        WHERE id_cliente = p_id_cliente;
        UPDATE empresas
        SET rubro = TRIM(p_rubro), director = TRIM(p_director)
        WHERE id_cliente = p_id_cliente;
        COMMIT;
        SET p_filas = 1;
    END IF;
END$$

CREATE PROCEDURE sp_listar_empresas()
BEGIN
    SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial,
           e.rubro, e.director
    FROM clientes c
    JOIN empresas e ON e.id_cliente = c.id_cliente
    ORDER BY c.id_cliente;
END$$

-- =====================================================================
-- USUARIOS
-- =====================================================================

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

-- Alta: falla con mensaje claro si los datos están vacíos o el nombre ya existe.
CREATE PROCEDURE sp_alta_usuario(
    IN  p_usr        VARCHAR(255),
    IN  p_pwd        VARCHAR(255),
    OUT p_id_usuario INT
)
BEGIN
    CALL sp_validar_datos_usuario(p_usr, p_pwd);

    IF EXISTS (SELECT 1 FROM usuarios WHERE usr = TRIM(p_usr)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre de usuario ya existe.';
    END IF;

    INSERT INTO usuarios (usr, pwd) VALUES (TRIM(p_usr), p_pwd);
    SET p_id_usuario = LAST_INSERT_ID();
END$$

-- Modificación: no permite tomar el nombre de otro usuario.
CREATE PROCEDURE sp_modificar_usuario(
    IN  p_id_usuario INT,
    IN  p_usr        VARCHAR(255),
    IN  p_pwd        VARCHAR(255),
    OUT p_filas      INT
)
BEGIN
    CALL sp_validar_datos_usuario(p_usr, p_pwd);

    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = p_id_usuario) THEN
        SET p_filas = 0;
    ELSEIF EXISTS (SELECT 1 FROM usuarios
                   WHERE usr = TRIM(p_usr) AND id_usuario <> p_id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre de usuario ya existe.';
    ELSE
        UPDATE usuarios SET usr = TRIM(p_usr), pwd = p_pwd
        WHERE id_usuario = p_id_usuario;
        SET p_filas = 1;
    END IF;
END$$

-- Listado sin exponer la clave.
CREATE PROCEDURE sp_listar_usuarios()
BEGIN
    SELECT id_usuario, usr
    FROM usuarios
    ORDER BY id_usuario;
END$$

-- Baja: no permite borrar el último usuario, para que siempre se pueda
-- volver a entrar al sistema.
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
