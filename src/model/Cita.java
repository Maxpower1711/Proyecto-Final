package model;

public class Cita {
    private String nroCita;
    private Cliente cliente;   // <-- Usa Cliente
    private Mascota mascota;   // <-- Usa Mascota
    private String sintomas;
    private String estado;

    public Cita(String nroCita, Cliente cliente, Mascota mascota, String sintomas) {
        this.nroCita = nroCita;
        this.cliente = cliente;
        this.mascota = mascota;
        this.sintomas = sintomas;
        this.estado = "No atendida";
    }
    
    public String getNroCita() { 
        return nroCita; 
    }
    public Cliente getCliente() { 
        return cliente; 
    }
    public Mascota getMascota() { 
        return mascota; 
    }
    public String getSintomas() { 
        return sintomas; 
    }
    public String getEstado() {
        return estado;
    }

    public void setCliente(Cliente cliente) { 
        this.cliente = cliente; 
    }
    public void setMascota(Mascota mascota) { 
        this.mascota = mascota; 
    }
    public void setSintomas(String sintomas) { 
        this.sintomas = sintomas; 
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
}
