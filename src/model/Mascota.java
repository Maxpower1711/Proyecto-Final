package model;

//clase padre
public class Mascota {
    
    // Atributos privados
    private int codigo;
    private int edad;
    private String raza;
    private double peso;
    private int numeroVacunas;

    // Constructor explícito
    public Mascota(int codigo, int edad ,String raza, double peso, int numeroVacunas) {
        this.codigo = codigo;
        this.edad = edad;
        this.raza = raza;
        this.peso = peso;
        this.numeroVacunas = numeroVacunas;
    }

    // Métodos get y set
    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
    
    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getNumeroVacunas() {
        return numeroVacunas;
    }

    public void setNumeroVacunas(int numeroVacunas) {
        this.numeroVacunas = numeroVacunas;
    }

}
