package com.example.servicio;

import com.example.Empresa;
import com.example.data.EmpresaData;

import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de empresas. Los datos se validan en los stored procedures:
 * si algo es inválido llega una SQLException con el motivo en getMessage().
 */
public class EmpresaServicio {

    private final EmpresaData data = new EmpresaData();

    /** Alta. Devuelve el id generado y también lo asigna en el objeto. */
    public int alta(Empresa empresa) throws SQLException {
        if (empresa == null) {
            throw new IllegalArgumentException("La empresa no puede ser nula.");
        }
        int id = data.alta(empresa);
        empresa.setId(id);
        return id;
    }

    /** Modificación. Devuelve false si el id no corresponde a una empresa. */
    public boolean modificar(Empresa empresa) throws SQLException {
        if (empresa == null || empresa.getId() <= 0) {
            throw new IllegalArgumentException("La empresa a modificar tiene que tener un id válido.");
        }
        return data.modificar(empresa) > 0;
    }

    public List<Empresa> listar() throws SQLException {
        return data.listar();
    }
}
