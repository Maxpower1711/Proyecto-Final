package controller;

import java.util.ArrayList;
import model.Cita;

public class ArregloCita {
    private ArrayList<Cita> citas;

    public ArregloCita() {
        citas = new ArrayList<>();
    }

    public void adicionar(Cita cita) {
        citas.add(cita);
    }

    public int tamaño() {
        return citas.size();
    }

    public Cita obtener(int i) {
        return citas.get(i);
    }

    public Cita buscar(String nroCita) {
        for (Cita c : citas) {
            if (c.getNroCita().equalsIgnoreCase(nroCita)) {
                return c;
            }
        }
        return null;
    }

    public void eliminar(Cita cita) {
        citas.remove(cita);
    }
}
