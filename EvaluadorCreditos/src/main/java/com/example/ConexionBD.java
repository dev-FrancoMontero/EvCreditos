package com.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String url = "jdbc:mysql://localhost:3306/Creditos";
    private static final String user = "root";
    private static final String password = "root";
    private static Connection conexion = null;

    private ConexionBD() {
    }
    
    public static Connection getConnection() {
        if (conexion == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException("Error al conectar a la base de datos", e);
            }
        }
        return conexion;
    }



}