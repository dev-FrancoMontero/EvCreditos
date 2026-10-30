package com.example;

/**
 * Datos comunes a todo cliente. Cada subtipo define su propia regla
 * de evaluación en {@link #evaluarRiesgo()}.
 */
public abstract class Cliente {
    private int id;
    private String nombre;
    private double ingresos;
    private double gastos;
    private boolean tieneBuenHistorial;

    protected Cliente(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial) {
        this.nombre = nombre;
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.tieneBuenHistorial = tieneBuenHistorial;
    }

    /**
     * Evalúa el perfil financiero del cliente.
     *
     * @return true si el cliente es apto para recibir el préstamo
     */
    public abstract boolean evaluarRiesgo();

    /** Nombre del tipo de cliente para mostrar en pantalla. */
    public abstract String getTipo();

    /** 0 mientras el cliente no se haya guardado en la base. */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getIngresos() {
        return ingresos;
    }

    public void setIngresos(double ingresos) {
        this.ingresos = ingresos;
    }

    public double getGastos() {
        return gastos;
    }

    public void setGastos(double gastos) {
        this.gastos = gastos;
    }

    public boolean isTieneBuenHistorial() {
        return tieneBuenHistorial;
    }

    public void setTieneBuenHistorial(boolean tieneBuenHistorial) {
        this.tieneBuenHistorial = tieneBuenHistorial;
    }
}
