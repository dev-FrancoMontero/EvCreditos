package com.example;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Login y administración de usuarios. Todo el acceso a la base pasa por
 * stored procedures.
 *
 * Las validaciones (campos vacíos, usuario duplicado, borrar el último usuario)
 * las hace la base: llegan como SQLException con el motivo en getMessage().
 */
public class UsuarioServicio {

    /** true solo si el usuario y la clave coinciden exactamente. */
    public boolean validarAcceso(String usuario, String clave) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_validar_login(?, ?, ?)}")) {
            cs.setString(1, usuario);
            cs.setString(2, clave);
            cs.registerOutParameter(3, Types.BOOLEAN);
            cs.execute();
            return cs.getBoolean(3);
        }
    }

    /**
     * Crea un usuario nuevo.
     *
     * @return el id generado
     * @throws SQLException si faltan datos o el nombre ya existe
     */
    public int crearUsuario(String usuario, String clave) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_crear_usuario(?, ?, ?)}")) {
            cs.setString(1, usuario);
            cs.setString(2, clave);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        }
    }

    /** Lista los usuarios (id y nombre, sin la clave). */
    public List<Usuario> listarUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_listar_usuarios()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Usuario(rs.getInt("id_usuario"), rs.getString("usr")));
            }
        }
        return usuarios;
    }

    /**
     * Elimina un usuario por su id.
     *
     * @return true si se eliminó; false si el id no existía
     * @throws SQLException si es el último usuario del sistema
     */
    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_eliminar_usuario(?, ?)}")) {
            cs.setInt(1, idUsuario);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2) > 0;
        }
    }
}
