package com.example.servicio;

import com.example.Usuario;
import com.example.data.UsuarioData;

import java.sql.SQLException;
import java.util.List;

/**
 * Login y ABML de usuarios. Las validaciones (campos vacíos, nombre
 * duplicado, borrar el último usuario) las hacen los stored procedures:
 * llegan como SQLException con el motivo en getMessage().
 */
public class UsuarioServicio {

    private final UsuarioData data = new UsuarioData();

    /** true solo si usuario y clave coinciden exactamente. */
    public boolean iniciarSesion(String usuario, String clave) throws SQLException {
        return data.validarLogin(usuario, clave);
    }

    /** Alta. Devuelve el id generado. */
    public int alta(String usuario, String clave) throws SQLException {
        return data.alta(usuario, clave);
    }

    /** Modificación. Devuelve false si el id no existía. */
    public boolean modificar(int idUsuario, String usuario, String clave) throws SQLException {
        return data.modificar(idUsuario, usuario, clave) > 0;
    }

    /** Baja por id. Devuelve false si el id no existía. */
    public boolean baja(int idUsuario) throws SQLException {
        return data.eliminar(idUsuario) > 0;
    }

    /** Listado sin las claves. */
    public List<Usuario> listar() throws SQLException {
        return data.listar();
    }
}
