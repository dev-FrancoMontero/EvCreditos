package com.example;

import com.example.servicio.ClienteServicio;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {

    private static final String PERSONA_FISICA = "Persona Física";
    private static final String EMPRESA = "Empresa";
    private static final int MAXIMO_MONTO = 1_000_000_000;

    // Formulario
    private final JComboBox<String> comboTipoCliente = new JComboBox<>(new String[]{PERSONA_FISICA, EMPRESA});
    private final JTextField campoNombre = new JTextField(20);
    private final JSpinner spinnerIngresos = new JSpinner(new SpinnerNumberModel(0, 0, MAXIMO_MONTO, 1000));
    private final JSpinner spinnerGastos = new JSpinner(new SpinnerNumberModel(0, 0, MAXIMO_MONTO, 1000));
    private final JCheckBox checkHistorial = new JCheckBox("Tiene buen historial crediticio");
    private final JLabel etiquetaRubro = new JLabel("Rubro:");
    private final JTextField campoRubro = new JTextField(20);
    private final JLabel etiquetaDirector = new JLabel("Director:");
    private final JTextField campoDirector = new JTextField(20);

    // Listado de clientes
    private final DefaultTableModel modeloClientes = new DefaultTableModel(
            new Object[]{"ID", "Tipo", "Nombre", "Ingresos", "Gastos", "Buen historial", "Rubro", "Director", "Riesgo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JProgressBar barraProgreso = new JProgressBar();

    // Acciones compartidas entre menú, barra de herramientas y botones
    private final Action accionGuardar = new AbstractAction("Guardar") {
        @Override
        public void actionPerformed(ActionEvent e) {
            guardarCliente();
        }
    };
    private final Action accionRefrescar = new AbstractAction("Refrescar") {
        @Override
        public void actionPerformed(ActionEvent e) {
            refrescarClientes();
        }
    };
    private final Action accionUsuarios = new AbstractAction("Usuarios") {
        @Override
        public void actionPerformed(ActionEvent e) {
            abrirVentanaUsuarios();
        }
    };
    private final Action accionSalir = new AbstractAction("Salir") {
        @Override
        public void actionPerformed(ActionEvent e) {
            salir();
        }
    };

    private final ClienteServicio clienteServicio = new ClienteServicio();
    private VentanaUsuarios ventanaUsuarios;

    public VentanaPrincipal(String usuarioLogueado) {
        super("Evaluador de Créditos - " + usuarioLogueado);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 520);
        setLocationRelativeTo(null);

        setJMenuBar(crearMenu());
        add(crearBarraHerramientas(), BorderLayout.NORTH);

        JTabbedPane panelPestanias = new JTabbedPane();
        panelPestanias.addTab("Nuevo análisis", crearFormulario());
        panelPestanias.addTab("Clientes", crearListadoClientes());
        add(panelPestanias, BorderLayout.CENTER);

        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");
        add(barraProgreso, BorderLayout.SOUTH);

        comboTipoCliente.addActionListener(e -> actualizarCamposSegunTipo());
        actualizarCamposSegunTipo();

        refrescarClientes();
    }

    private JMenuBar crearMenu() {
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.add(new JMenuItem(accionGuardar));
        menuArchivo.add(new JMenuItem(accionRefrescar));
        menuArchivo.addSeparator();
        menuArchivo.add(new JMenuItem(accionSalir));

        JMenu menuAdministracion = new JMenu("Administración");
        menuAdministracion.add(new JMenuItem(accionUsuarios));

        barraMenu.add(menuArchivo);
        barraMenu.add(menuAdministracion);
        return barraMenu;
    }

    private JToolBar crearBarraHerramientas() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(accionGuardar);
        barra.add(accionRefrescar);
        barra.addSeparator();
        barra.add(accionUsuarios);
        return barra;
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        int fila = 0;
        agregarFila(panel, fila++, new JLabel("Tipo de cliente:"), comboTipoCliente);
        agregarFila(panel, fila++, new JLabel("Nombre:"), campoNombre);
        agregarFila(panel, fila++, new JLabel("Ingresos:"), spinnerIngresos);
        agregarFila(panel, fila++, new JLabel("Gastos:"), spinnerGastos);
        agregarFila(panel, fila++, new JLabel(""), checkHistorial);
        agregarFila(panel, fila++, etiquetaRubro, campoRubro);
        agregarFila(panel, fila++, etiquetaDirector, campoDirector);

        JButton botonGuardar = new JButton(accionGuardar);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = fila++;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(12, 4, 4, 4);
        panel.add(botonGuardar, c);

        // Empuja el formulario hacia arriba
        c = new GridBagConstraints();
        c.gridy = fila;
        c.weighty = 1;
        panel.add(new JLabel(), c);

        return panel;
    }

    private void agregarFila(JPanel panel, int fila, JComponent etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 12);
        panel.add(etiqueta, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);
        panel.add(campo, c);
    }

    private JPanel crearListadoClientes() {
        JTable tabla = new JTable(modeloClientes);
        tabla.setAutoCreateRowSorter(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // Muestra rubro y director solo cuando el tipo es Empresa
    private void actualizarCamposSegunTipo() {
        boolean esEmpresa = EMPRESA.equals(comboTipoCliente.getSelectedItem());
        etiquetaRubro.setVisible(esEmpresa);
        campoRubro.setVisible(esEmpresa);
        etiquetaDirector.setVisible(esEmpresa);
        campoDirector.setVisible(esEmpresa);
        revalidate();
        repaint();
    }

    // Arma la instancia de Cliente que corresponde al tipo elegido
    private Cliente crearCliente(boolean esEmpresa, String nombre, double ingresos, double gastos,
                                 boolean historial, String rubro, String director) {
        if (esEmpresa) {
            return new Empresa(nombre, ingresos, gastos, historial, rubro, director);
        }
        return new PersonaFisica(nombre, ingresos, gastos, historial);
    }

    // Resultado de evaluarRiesgo() para mostrar en pantalla
    private String textoRiesgo(Cliente cliente) {
        return clienteServicio.evaluar(cliente) ? "Apto" : "No apto";
    }

    private void guardarCliente() {
        String nombre = campoNombre.getText().trim();
        int ingresos = (Integer) spinnerIngresos.getValue();
        int gastos = (Integer) spinnerGastos.getValue();
        boolean historial = checkHistorial.isSelected();
        boolean esEmpresa = EMPRESA.equals(comboTipoCliente.getSelectedItem());
        String rubro = campoRubro.getText().trim();
        String director = campoDirector.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del cliente.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (esEmpresa && (rubro.isEmpty() || director.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Ingrese el rubro y el director de la empresa.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente cliente = crearCliente(esEmpresa, nombre, ingresos, gastos, historial, rubro, director);

        iniciarCarga("Guardando cliente...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return clienteServicio.alta(cliente);
            }

            @Override
            protected void done() {
                finalizarCarga();
                try {
                    int id = get();
                    JOptionPane.showMessageDialog(VentanaPrincipal.this,
                            "Cliente guardado con ID " + id + ".\nRiesgo evaluado: " + textoRiesgo(cliente),
                            "Análisis completado", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    refrescarClientes();
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError("No se pudo guardar el cliente", ex);
                }
            }
        }.execute();
    }

    private void refrescarClientes() {
        iniciarCarga("Cargando clientes...");
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return consultarClientes();
            }

            @Override
            protected void done() {
                finalizarCarga();
                try {
                    modeloClientes.setRowCount(0);
                    for (Object[] fila : get()) {
                        modeloClientes.addRow(fila);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError("No se pudo cargar el listado de clientes", ex);
                }
            }
        }.execute();
    }

    // ---------- Acceso a la base (a través de ClienteServicio) ----------

    // Convierte cada cliente en una fila de la tabla
    private List<Object[]> consultarClientes() throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        for (Cliente cliente : clienteServicio.listar()) {
            Empresa empresa = cliente instanceof Empresa e ? e : null;
            filas.add(new Object[]{
                    cliente.getId(),
                    cliente.getTipo(),
                    cliente.getNombre(),
                    cliente.getIngresos(),
                    cliente.getGastos(),
                    cliente.isTieneBuenHistorial() ? "Sí" : "No",
                    empresa != null ? empresa.getRubro() : "",
                    empresa != null ? empresa.getDirector() : "",
                    textoRiesgo(cliente)
            });
        }
        return filas;
    }

    // ---------- Otras acciones ----------

    private void abrirVentanaUsuarios() {
        if (ventanaUsuarios != null && ventanaUsuarios.isDisplayable()) {
            ventanaUsuarios.toFront();
            return;
        }
        iniciarCarga("Abriendo gestión de usuarios...");
        ventanaUsuarios = new VentanaUsuarios(this::finalizarCarga);
        ventanaUsuarios.setVisible(true);
    }

    private void salir() {
        int opcion = JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void limpiarFormulario() {
        comboTipoCliente.setSelectedIndex(0);
        campoNombre.setText("");
        spinnerIngresos.setValue(0);
        spinnerGastos.setValue(0);
        checkHistorial.setSelected(false);
        campoRubro.setText("");
        campoDirector.setText("");
    }

    private void iniciarCarga(String mensaje) {
        barraProgreso.setIndeterminate(true);
        barraProgreso.setString(mensaje);
        accionGuardar.setEnabled(false);
        accionRefrescar.setEnabled(false);
        accionUsuarios.setEnabled(false);
    }

    private void finalizarCarga() {
        barraProgreso.setIndeterminate(false);
        barraProgreso.setString("Listo");
        accionGuardar.setEnabled(true);
        accionRefrescar.setEnabled(true);
        accionUsuarios.setEnabled(true);
    }

    private void mostrarError(String mensaje, Exception ex) {
        Throwable causa = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + causa.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
