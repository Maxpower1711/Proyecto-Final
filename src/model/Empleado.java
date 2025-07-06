package model;

public class Empleado {
    // Atributos privados
    private int codigo;
    private String nombres;
    private String cargo;
    private double sueldo;

    // Constructor explícito
    public Empleado(int codigo, String nombres, String cargo, double sueldo) {
        this.codigo = codigo;
        this.nombres = nombres;
        this.cargo = cargo;
        this.sueldo = sueldo;
    }

    // Métodos get y set
    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }
}
