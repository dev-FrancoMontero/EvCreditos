package com.example;

import com.example.servicio.UsuarioServicio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class VentanaUsuarios extends JFrame {

    private static final int COLUMNA_ID = 0;

    private final JTextField campoNombreUsuario = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);
    private final JButton botonCrearUsuario = new JButton("Crear usuario");
    private final JButton botonEliminarUsuario = new JButton("Eliminar");
    private final JButton botonRefrescar = new JButton("Refrescar");
    private final JProgressBar barraProgreso = new JProgressBar();
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    // El id queda en el modelo aunque su columna no se muestre: lo necesita la eliminación
    private final DefaultTableModel modeloUsuarios = new DefaultTableModel(new Object[]{"ID", "Usuario"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == COLUMNA_ID ? Integer.class : String.class;
        }
    };
    private final JTable tablaUsuarios = new JTable(modeloUsuarios);

    public VentanaUsuarios(Runnable alTerminarCargaInicial) {
        super("Gestión de usuarios");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 460);
        setLocationRelativeTo(null);

        // VERTICAL_SPLIT: el divisor es horizontal, formulario arriba y listado abajo
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearPanelAlta(), crearPanelListado());
        split.setDividerLocation(140);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");
        add(barraProgreso, BorderLayout.SOUTH);

        refrescarListado(alTerminarCargaInicial);
    }

    private JPanel crearPanelAlta() {
        JPanel campos = new JPanel(new GridLayout(2, 2, 8, 8));
        campos.add(new JLabel("Nombre de usuario:"));
        campos.add(campoNombreUsuario);
        campos.add(new JLabel("Clave:"));
        campos.add(campoClave);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(botonCrearUsuario);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo usuario"));
        panel.add(campos, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);

        botonCrearUsuario.addActionListener(e -> crearUsuario());
        return panel;
    }

    private JPanel crearPanelListado() {
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloUsuarios);
        tablaUsuarios.setRowSorter(sorter);

        // Oculta la columna ID solo de la vista; el modelo la conserva
        tablaUsuarios.removeColumn(tablaUsuarios.getColumnModel().getColumn(COLUMNA_ID));

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenuContextual(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenuContextual(e);
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(botonRefrescar);
        botones.add(botonEliminarUsuario);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Usuarios existentes"));
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        botonEliminarUsuario.addActionListener(e -> eliminarSeleccionado());
        botonRefrescar.addActionListener(e -> refrescarListado(null));
        return panel;
    }

    private void mostrarMenuContextual(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        // Selecciona la fila sobre la que se hizo clic derecho
        int fila = tablaUsuarios.rowAtPoint(e.getPoint());
        if (fila >= 0) {
            tablaUsuarios.setRowSelectionInterval(fila, fila);
        }

        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemEliminar = new JMenuItem("Eliminar usuario");
        itemEliminar.addActionListener(ev -> eliminarSeleccionado());
        JMenuItem itemRefrescar = new JMenuItem("Refrescar listado");
        itemRefrescar.addActionListener(ev -> refrescarListado(null));
        menu.add(itemEliminar);
        menu.add(itemRefrescar);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void crearUsuario() {
        String nombre = campoNombreUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete el nombre de usuario y la clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        iniciarCarga("Creando usuario...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                usuarioServicio.alta(nombre, clave);
                return null;
            }

            @Override
            protected void done() {
                finalizarCarga();
                try {
                    get();
                    campoNombreUsuario.setText("");
                    campoClave.setText("");
                    refrescarListado(null);
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError("No se pudo crear el usuario", ex);
                }
            }
        }.execute();
    }

    // Misma rutina para el botón y para el menú contextual
    private void eliminarSeleccionado() {
        int filaVista = tablaUsuarios.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Con el sorter activo, el índice de la vista no coincide con el del modelo
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        int id = (Integer) modeloUsuarios.getValueAt(filaModelo, COLUMNA_ID);
        String nombre = (String) modeloUsuarios.getValueAt(filaModelo, 1);

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario \"" + nombre + "\"?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        iniciarCarga("Eliminando usuario...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return usuarioServicio.baja(id);
            }

            @Override
            protected void done() {
                finalizarCarga();
                try {
                    if (!get()) {
                        JOptionPane.showMessageDialog(VentanaUsuarios.this,
                                "El usuario ya no existía en la base de datos.",
                                "Sin cambios", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError("No se pudo eliminar el usuario", ex);
                }
                refrescarListado(null);
            }
        }.execute();
    }

    private void refrescarListado(Runnable alTerminar) {
        iniciarCarga("Cargando usuarios...");
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return listarUsuarios();
            }

            @Override
            protected void done() {
                finalizarCarga();
                try {
                    modeloUsuarios.setRowCount(0);
                    for (Object[] fila : get()) {
                        modeloUsuarios.addRow(fila);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError("No se pudo cargar el listado de usuarios", ex);
                } finally {
                    if (alTerminar != null) {
                        alTerminar.run();
                    }
                }
            }
        }.execute();
    }

    // ---------- Acceso a la base (a través de UsuarioServicio) ----------

    // Devuelve id y nombre de usuario, sin la clave
    private List<Object[]> listarUsuarios() throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        for (Usuario usuario : usuarioServicio.listar()) {
            filas.add(new Object[]{usuario.getId(), usuario.getNombre()});
        }
        return filas;
    }

    private void iniciarCarga(String mensaje) {
        barraProgreso.setIndeterminate(true);
        barraProgreso.setString(mensaje);
        botonCrearUsuario.setEnabled(false);
        botonEliminarUsuario.setEnabled(false);
        botonRefrescar.setEnabled(false);
    }

    private void finalizarCarga() {
        barraProgreso.setIndeterminate(false);
        barraProgreso.setString("Listo");
        botonCrearUsuario.setEnabled(true);
        botonEliminarUsuario.setEnabled(true);
        botonRefrescar.setEnabled(true);
    }

    private void mostrarError(String mensaje, Exception ex) {
        Throwable causa = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + causa.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
