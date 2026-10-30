package com.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Punto de entrada de la interfaz. La lógica está en el paquete
        // com.example.servicio y el acceso a la base en com.example.data.
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
