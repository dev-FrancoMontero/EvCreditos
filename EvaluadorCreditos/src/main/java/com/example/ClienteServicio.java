package com.example;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones sobre clientes. Todo el acceso a la base pasa por stored procedures.
 *
 * Las validaciones las hace la base: si un dato es inválido se lanza una
 * SQLException cuyo getMessage() trae el motivo, listo para mostrar al usuario.
 */
public class ClienteServicio {

    /**
     * Guarda el cliente en la tabla clientes y en la tabla de su subtipo.
     *
     * @return el id generado (también queda asignado en el objeto)
     */
    public int guardarCliente(Cliente cliente) throws SQLException {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }

        try (Connection conn = ConexionBD.getConnection()) {
            int id;
            if (cliente instanceof Empresa empresa) {
                try (CallableStatement cs = conn.prepareCall("{call sp_guardar_empresa(?, ?, ?, ?, ?, ?, ?)}")) {
                    cargarDatosComunes(cs, empresa);
                    cs.setString(5, empresa.getRubro());
                    cs.setString(6, empresa.getDirector());
                    cs.registerOutParameter(7, Types.INTEGER);
                    cs.execute();
                    id = cs.getInt(7);
                }
            } else {
                try (CallableStatement cs = conn.prepareCall("{call sp_guardar_persona_fisica(?, ?, ?, ?, ?)}")) {
                    cargarDatosComunes(cs, cliente);
                    cs.registerOutParameter(5, Types.INTEGER);
                    cs.execute();
                    id = cs.getInt(5);
                }
            }
            cliente.setId(id);
            return id;
        }
    }

    /** Devuelve todos los clientes guardados, cada uno con su subtipo correcto. */
    public List<Cliente> listarClientes() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_listar_clientes()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
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
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    /**
     * Elimina un cliente por su id.
     *
     * @return true si se eliminó; false si el id no existía
     */
    public boolean eliminarCliente(int idCliente) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_eliminar_cliente(?, ?)}")) {
            cs.setInt(1, idCliente);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2) > 0;
        }
    }

    private void cargarDatosComunes(CallableStatement cs, Cliente cliente) throws SQLException {
        cs.setString(1, cliente.getNombre());
        cs.setDouble(2, cliente.getIngresos());
        cs.setDouble(3, cliente.getGastos());
        cs.setBoolean(4, cliente.isTieneBuenHistorial());
    }
}
