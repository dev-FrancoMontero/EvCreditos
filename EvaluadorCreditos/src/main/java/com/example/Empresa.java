package com.example;

public class Empresa extends Cliente {

    /** Ratio ingresos / gastos mínimo para ser apta. */
    public static final double RATIO_MINIMO = 1.5;

    private String rubro;
    private String director;

    public Empresa(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial,
                   String rubro, String director) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
        this.rubro = rubro;
        this.director = director;
    }

    /**
     * Es apta si ingresos / gastos es al menos 1.5. Si los gastos son 0 no se
     * divide: es apta siempre que tenga algún ingreso.
     */
    @Override
    public boolean evaluarRiesgo() {
        if (getGastos() <= 0) {
            return getIngresos() > 0;
        }
        double ratio = getIngresos() / getGastos();
        return ratio >= RATIO_MINIMO;
    }

    @Override
    public String getTipo() {
        return "Empresa";
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }
}
