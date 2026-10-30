package com.example.servicio;

import com.example.Cliente;
import com.example.Empresa;
import com.example.PersonaFisica;
import com.example.data.ClienteData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica general de clientes: recibe cualquier tipo de Cliente y delega en
 * el servicio de su subtipo. Es la clase que conviene usar desde la interfaz.
 *
 * Los datos se validan en los stored procedures: si algo es inválido llega
 * una SQLException con el motivo en getMessage(), listo para un JOptionPane.
 */
public class ClienteServicio {

    private final ClienteData data = new ClienteData();
    private final PersonaFisicaServicio personaFisicaServicio = new PersonaFisicaServicio();
    private final EmpresaServicio empresaServicio = new EmpresaServicio();

    /** Alta de un cliente de cualquier tipo. Devuelve el id generado. */
    public int alta(Cliente cliente) throws SQLException {
        if (cliente instanceof Empresa empresa) {
            return empresaServicio.alta(empresa);
        }
        if (cliente instanceof PersonaFisica persona) {
            return personaFisicaServicio.alta(persona);
        }
        throw new IllegalArgumentException("Tipo de cliente no soportado.");
    }

    /** Modificación de un cliente de cualquier tipo. Devuelve false si el id no existía. */
    public boolean modificar(Cliente cliente) throws SQLException {
        if (cliente instanceof Empresa empresa) {
            return empresaServicio.modificar(empresa);
        }
        if (cliente instanceof PersonaFisica persona) {
            return personaFisicaServicio.modificar(persona);
        }
        throw new IllegalArgumentException("Tipo de cliente no soportado.");
    }

    /** Baja por id. Devuelve false si el id no existía. */
    public boolean baja(int idCliente) throws SQLException {
        return data.eliminar(idCliente) > 0;
    }

    /** Todos los clientes, cada uno con su subtipo. */
    public List<Cliente> listar() throws SQLException {
        return data.listar();
    }

    /** El cliente con ese id, o null si no existe. */
    public Cliente buscarPorId(int idCliente) throws SQLException {
        return data.buscarPorId(idCliente);
    }

    /** true si el cliente es apto para el préstamo, según la regla de su tipo. */
    public boolean evaluar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        return cliente.evaluarRiesgo();
    }

    /** Solo los clientes aptos. */
    public List<Cliente> listarAptos() throws SQLException {
        List<Cliente> aptos = new ArrayList<>();
        for (Cliente cliente : data.listar()) {
            if (cliente.evaluarRiesgo()) {
                aptos.add(cliente);
            }
        }
        return aptos;
    }
}
