package model;

public class Cliente {
    // Atributos privados
    private String dni;
    private String nombre;
    private int edad;
    private int codigoMascota;
    
    public Cliente(String dni, String nombre, int edad, int codigoMascota) {
        this.dni = dni;
        this.nombre = nombre;
        this.edad = edad;
        this.codigoMascota = codigoMascota;
    }
    
    // Métodos get y set
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public int getCodigoMascota() {
        return codigoMascota;
    }

    public void setCodigoMascota(int codigoMascota) {
        this.codigoMascota = codigoMascota;
    }
}
