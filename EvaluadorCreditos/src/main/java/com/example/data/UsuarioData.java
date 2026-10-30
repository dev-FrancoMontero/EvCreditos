package com.example.data;

import com.example.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la base para usuarios y login. */
public class UsuarioData {

    /** sp_validar_login: true solo si usuario y clave coinciden exactamente. */
    public boolean validarLogin(String usuario, String clave) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_validar_login(?, ?, ?)}")) {
            cs.setString(1, usuario);
            cs.setString(2, clave);
            cs.registerOutParameter(3, Types.BOOLEAN);
            cs.execute();
            return cs.getBoolean(3);
        }
    }

    /** sp_alta_usuario: devuelve el id_usuario generado. */
    public int alta(String usuario, String clave) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_alta_usuario(?, ?, ?)}")) {
            cs.setString(1, usuario);
            cs.setString(2, clave);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        }
    }

    /** sp_modificar_usuario: devuelve las filas modificadas (0 si el id no existía). */
    public int modificar(int idUsuario, String usuario, String clave) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_modificar_usuario(?, ?, ?, ?)}")) {
            cs.setInt(1, idUsuario);
            cs.setString(2, usuario);
            cs.setString(3, clave);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            return cs.getInt(4);
        }
    }

    /** sp_listar_usuarios: id y nombre, sin la clave. */
    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_listar_usuarios()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Usuario(rs.getInt("id_usuario"), rs.getString("nombre_usuario")));
            }
        }
        return usuarios;
    }

    /** sp_eliminar_usuario: devuelve las filas eliminadas (0 si el id no existía). */
    public int eliminar(int idUsuario) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_eliminar_usuario(?, ?)}")) {
            cs.setInt(1, idUsuario);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
        }
    }
}
