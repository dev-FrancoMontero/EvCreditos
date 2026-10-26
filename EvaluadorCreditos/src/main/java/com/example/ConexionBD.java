package com.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String url = "jdbc:mysql://localhost:3306/Creditos";
    private static final String user = "root";
    private static final String password = "12341234";

    private ConexionBD() {
    }

    /**
     * Abre una conexión nueva. Quien la pide la cierra con try-with-resources,
     * así se libera aunque ocurra una excepción.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
