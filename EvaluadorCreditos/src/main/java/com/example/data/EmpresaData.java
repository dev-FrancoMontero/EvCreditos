package com.example.data;

import com.example.Empresa;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la base para empresas. */
public class EmpresaData {

    /** sp_alta_empresa: devuelve el id_cliente generado. */
    public int alta(Empresa empresa) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_alta_empresa(?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, empresa.getNombre());
            cs.setDouble(2, empresa.getIngresos());
            cs.setDouble(3, empresa.getGastos());
            cs.setBoolean(4, empresa.isTieneBuenHistorial());
            cs.setString(5, empresa.getRubro());
            cs.setString(6, empresa.getDirector());
            cs.registerOutParameter(7, Types.INTEGER);
            cs.execute();
            return cs.getInt(7);
        }
    }

    /** sp_modificar_empresa: devuelve las filas modificadas (0 si el id no es una empresa). */
    public int modificar(Empresa empresa) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_modificar_empresa(?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, empresa.getId());
            cs.setString(2, empresa.getNombre());
            cs.setDouble(3, empresa.getIngresos());
            cs.setDouble(4, empresa.getGastos());
            cs.setBoolean(5, empresa.isTieneBuenHistorial());
            cs.setString(6, empresa.getRubro());
            cs.setString(7, empresa.getDirector());
            cs.registerOutParameter(8, Types.INTEGER);
            cs.execute();
            return cs.getInt(8);
        }
    }

    /** sp_listar_empresas. */
    public List<Empresa> listar() throws SQLException {
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_listar_empresas()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                Empresa empresa = new Empresa(
                        rs.getString("nombre"),
                        rs.getDouble("ingresos"),
                        rs.getDouble("gastos"),
                        rs.getBoolean("historial"),
                        rs.getString("rubro"),
                        rs.getString("director"));
                empresa.setId(rs.getInt("id_cliente"));
                empresas.add(empresa);
            }
        }
        return empresas;
    }
}
