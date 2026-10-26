package com.example.servicio;

import com.example.PersonaFisica;
import com.example.data.PersonaFisicaData;

import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de personas físicas. Los datos se validan en los stored procedures:
 * si algo es inválido llega una SQLException con el motivo en getMessage().
 */
public class PersonaFisicaServicio {

    private final PersonaFisicaData data = new PersonaFisicaData();

    /** Alta. Devuelve el id generado y también lo asigna en el objeto. */
    public int alta(PersonaFisica persona) throws SQLException {
        if (persona == null) {
            throw new IllegalArgumentException("La persona física no puede ser nula.");
        }
        int id = data.alta(persona);
        persona.setId(id);
        return id;
    }

    /** Modificación. Devuelve false si el id no corresponde a una persona física. */
    public boolean modificar(PersonaFisica persona) throws SQLException {
        if (persona == null || persona.getId() <= 0) {
            throw new IllegalArgumentException("La persona física a modificar tiene que tener un id válido.");
        }
        return data.modificar(persona) > 0;
    }

    public List<PersonaFisica> listar() throws SQLException {
        return data.listar();
    }
}
