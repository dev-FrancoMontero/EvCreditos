package com.example;

import java.sql.SQLException;

/**
 * Prueba por consola de la lógica y de los stored procedures, sin interfaz.
 * Requiere haber ejecutado sql/01_tablas.sql y sql/02_stored_procedures.sql.
 */
public class PruebaLogica {

    public static void main(String[] args) throws SQLException {
        ClienteServicio clientes = new ClienteServicio();
        UsuarioServicio usuarios = new UsuarioServicio();

        System.out.println("== Login ==");
        System.out.println("Bonillo / Pequenio  -> " + usuarios.validarAcceso("Bonillo", "Pequenio"));
        System.out.println("Bonillo / pequenio  -> " + usuarios.validarAcceso("Bonillo", "pequenio"));
        System.out.println("Bonillo / (vacía)   -> " + usuarios.validarAcceso("Bonillo", ""));

        System.out.println("\n== Clientes ==");
        Cliente persona = new PersonaFisica("Ana Gomez", 1000, 500, true);
        Cliente empresa = new Empresa("Acme SA", 3000, 1000, true, "Industria", "Juan Perez");
        System.out.println("Persona guardada con id " + clientes.guardarCliente(persona)
                + ", apta: " + persona.evaluarRiesgo());
        System.out.println("Empresa guardada con id " + clientes.guardarCliente(empresa)
                + ", apta: " + empresa.evaluarRiesgo());
        esperarError("Cliente sin nombre", () -> clientes.guardarCliente(new PersonaFisica("  ", 100, 50, true)));
        esperarError("Gastos negativos", () -> clientes.guardarCliente(new PersonaFisica("Luis", 100, -5, true)));
        esperarError("Empresa sin rubro", () -> clientes.guardarCliente(new Empresa("X SRL", 100, 50, true, "", "Pepe")));

        for (Cliente c : clientes.listarClientes()) {
            System.out.printf("  #%d %-12s %-15s apto=%s%n", c.getId(), c.getNombre(), c.getTipo(), c.evaluarRiesgo());
        }

        System.out.println("\n== Usuarios ==");
        int id = usuarios.crearUsuario("prueba", "1234");
        System.out.println("Usuario creado con id " + id);
        esperarError("Usuario duplicado", () -> usuarios.crearUsuario("prueba", "otra"));
        esperarError("Usuario vacío", () -> usuarios.crearUsuario("", "1234"));
        for (Usuario u : usuarios.listarUsuarios()) {
            System.out.println("  #" + u.getId() + " " + u.getNombre());
        }
        System.out.println("Eliminar id " + id + "     -> " + usuarios.eliminarUsuario(id));
        System.out.println("Eliminar id 99999 -> " + usuarios.eliminarUsuario(99999));
    }

    private interface Accion {
        void ejecutar() throws SQLException;
    }

    private static void esperarError(String caso, Accion accion) {
        try {
            accion.ejecutar();
            System.out.println(caso + " -> ERROR: se esperaba una excepción");
        } catch (SQLException e) {
            System.out.println(caso + " -> rechazado: " + e.getMessage());
        }
    }
}
