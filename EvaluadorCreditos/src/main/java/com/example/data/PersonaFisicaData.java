package com.example.data;

import com.example.PersonaFisica;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la base para personas físicas. */
public class PersonaFisicaData {

    /** sp_alta_persona_fisica: devuelve el id_cliente generado. */
    public int alta(PersonaFisica persona) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_alta_persona_fisica(?, ?, ?, ?, ?)}")) {
            cs.setString(1, persona.getNombre());
            cs.setDouble(2, persona.getIngresos());
            cs.setDouble(3, persona.getGastos());
            cs.setBoolean(4, persona.isTieneBuenHistorial());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            return cs.getInt(5);
        }
    }

    /** sp_modificar_persona_fisica: devuelve las filas modificadas (0 si el id no es una persona física). */
    public int modificar(PersonaFisica persona) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_modificar_persona_fisica(?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, persona.getId());
            cs.setString(2, persona.getNombre());
            cs.setDouble(3, persona.getIngresos());
            cs.setDouble(4, persona.getGastos());
            cs.setBoolean(5, persona.isTieneBuenHistorial());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();
            return cs.getInt(6);
        }
    }

    /** sp_listar_personas_fisicas. */
    public List<PersonaFisica> listar() throws SQLException {
        List<PersonaFisica> personas = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_listar_personas_fisicas()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                PersonaFisica persona = new PersonaFisica(
                        rs.getString("nombre"),
                        rs.getDouble("ingresos"),
                        rs.getDouble("gastos"),
                        rs.getBoolean("historial"));
                persona.setId(rs.getInt("id_cliente"));
                personas.add(persona);
            }
        }
        return personas;
    }
}
