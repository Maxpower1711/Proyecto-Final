package controller;

import java.util.ArrayList;
import model.Mascota;

public class ArregloMascota {
    private ArrayList<Mascota> masco;

    // Constructor sin parámetros que crea el ArrayList
    public ArregloMascota() {
        masco = new ArrayList<Mascota>();
    }

    // Método para adicionar una mascota
    public void adicionar(Mascota x) {
        masco.add(x);
    }

    // Método para eliminar una mascota
    public void eliminar(Mascota x) {
        masco.remove(x);
    }

    // Obtener mascota por posición
    public Mascota obtener(int pos) {
        return masco.get(pos);
    }

    // Buscar mascota por código
    public Mascota buscar(int codigo) {
        for (Mascota m : masco) {
            if (m.getCodigo() == codigo) {
                return m;
            }
        }
        return null;
    }

    // Tamaño del arreglo
    public int tamaño() {
        return masco.size();
    }
}
