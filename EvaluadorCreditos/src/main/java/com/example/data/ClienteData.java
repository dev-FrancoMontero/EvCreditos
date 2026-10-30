package com.example.data;

import com.example.Cliente;
import com.example.Empresa;
import com.example.PersonaFisica;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la base para operaciones comunes a todos los clientes. */
public class ClienteData {

    /** sp_listar_clientes: todos los clientes, cada uno con su subtipo. */
    public List<Cliente> listar() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_listar_clientes()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                clientes.add(leerCliente(rs));
            }
        }
        return clientes;
    }

    /** sp_buscar_cliente: el cliente con ese id, o null si no existe. */
    public Cliente buscarPorId(int idCliente) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_buscar_cliente(?)}")) {
            cs.setInt(1, idCliente);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? leerCliente(rs) : null;
            }
        }
    }

    /** sp_eliminar_cliente: devuelve las filas eliminadas (0 si el id no existía). */
    public int eliminar(int idCliente) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_eliminar_cliente(?, ?)}")) {
            cs.setInt(1, idCliente);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
        }
    }

    /** Arma el objeto correcto según la columna "tipo". */
    private Cliente leerCliente(ResultSet rs) throws SQLException {
        String nombre = rs.getString("nombre");
        double ingresos = rs.getDouble("ingresos");
        double gastos = rs.getDouble("gastos");
        boolean historial = rs.getBoolean("historial");

        Cliente cliente;
        if ("EMPRESA".equals(rs.getString("tipo"))) {
            cliente = new Empresa(nombre, ingresos, gastos, historial,
                    rs.getString("rubro"), rs.getString("director"));
        } else {
            cliente = new PersonaFisica(nombre, ingresos, gastos, historial);
        }
        cliente.setId(rs.getInt("id_cliente"));
        return cliente;
    }
}
