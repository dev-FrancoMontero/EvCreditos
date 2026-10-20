package com.example;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {

        public registrarUsuario(Cliente cliente) {
            Connection conn = ConexionBD.getConnection();

            String sql = "INSERT INTO clientes (nombre, gastos, ingresos, tieneBuenHistorial) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setInt(2, cliente.getGastos());
            ps.setInt(3, cliente.getIngresos());
            ps.setBoolean(4, cliente.isTieneBuenHistorial());
            ps.setString(5, cliente.getNombre());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new RuntimeException("El usuario ya existe en la base de datos");
                    }
                    else {
                        String id = 
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al registrar el usuario", e);
            }
            return 
        }
    }
}