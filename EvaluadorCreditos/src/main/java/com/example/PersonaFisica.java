package com.example;

public class PersonaFisica extends Cliente {

    /** Porción mínima de los ingresos que tiene que quedar libre después de los gastos. */
    public static final double MARGEN_MINIMO = 0.30;

    public PersonaFisica(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
    }

    /**
     * Sin buen historial no es apto. Con buen historial, es apto si el margen
     * (ingresos - gastos) es al menos el 30% de los ingresos.
     */
    @Override
    public boolean evaluarRiesgo() {
        if (!isTieneBuenHistorial() || getIngresos() <= 0) {
            return false;
        }
        double margen = getIngresos() - getGastos();
        return margen >= getIngresos() * MARGEN_MINIMO;
    }

    @Override
    public String getTipo() {
        return "Persona física";
    }
}
