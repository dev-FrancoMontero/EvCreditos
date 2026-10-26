package com.example;

public abstract class Cliente {
    private String nombre;
    private int gastos;
    private int ingresos;
    private boolean tieneBuenHistorial;
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public int getGastos() {
        return gastos;
    }
    public void setGastos(int gastos) {
        this.gastos = gastos;
    }
    public int getIngresos() {
        return ingresos;
    }
    public void setIngresos(int ingresos) {
        this.ingresos = ingresos;
    }
    public boolean isTieneBuenHistorial() {
        return tieneBuenHistorial;
    }
    public void setTieneBuenHistorial(boolean tieneBuenHistorial) {
        this.tieneBuenHistorial = tieneBuenHistorial;
    }

    public abstract int evaluarRiesgo();
}

class PersonaFisica extends Cliente {

    @Override 
    public int evaluarRiesgo() {
    
        if (isTieneBuenHistorial() == false) {
            return 0;
        }
        else {
            return getIngresos() - getGastos();
        }
    }
}

class Empresa extends Cliente {

    private String rubro;
    private String director;

    @Override 
    public int evaluarRiesgo() {
        int div = getIngresos() / getGastos();

        if (div < 1) {
            return 0;
        }
        else {
            return div;
        }
    }
}
