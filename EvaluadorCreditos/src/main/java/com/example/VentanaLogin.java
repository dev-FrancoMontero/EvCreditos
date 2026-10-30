package com.example;

import com.example.servicio.UsuarioServicio;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class VentanaLogin extends JFrame {

    private final JTextField campoUsuario = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);
    private final JButton botonIngresar = new JButton("Ingresar");
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    public VentanaLogin() {
        super("Evaluador de Créditos - Ingreso");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 160);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel grilla = new JPanel(new GridLayout(2, 2, 8, 8));
        grilla.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        grilla.add(new JLabel("Usuario:"));
        grilla.add(campoUsuario);
        grilla.add(new JLabel("Clave:"));
        grilla.add(campoClave);

        JPanel panelBoton = new JPanel();
        panelBoton.add(botonIngresar);

        add(grilla, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);

        botonIngresar.addActionListener(e -> ingresar());
        // Enter en cualquier campo dispara el ingreso
        getRootPane().setDefaultButton(botonIngresar);
    }

    private void ingresar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete usuario y clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (usuarioServicio.iniciarSesion(usuario, clave)) {
                dispose();
                new VentanaPrincipal(usuario).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o clave incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                campoClave.setText("");
                campoClave.requestFocusInWindow();
            }
        } catch (SQLException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con la base de datos:\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
