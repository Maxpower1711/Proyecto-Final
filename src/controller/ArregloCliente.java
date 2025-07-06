package controller;

import java.util.ArrayList;
import model.Cliente;

public class ArregloCliente {
    private ArrayList<Cliente> clientes;

    // Constructor que inicializa la lista
    public ArregloCliente() {
        clientes = new ArrayList<Cliente>();
    }

    // Método para adicionar un cliente
    public void adicionar(Cliente c) {
        clientes.add(c);
    }

    // Método para eliminar un cliente
    public void eliminar(Cliente c) {
        clientes.remove(c);
    }

    // Obtener cliente por posición
    public Cliente obtener(int pos) {
        return clientes.get(pos);
    }

    // Buscar cliente por DNI
    public Cliente buscarPorDni(String dni) {
        for (Cliente c : clientes) {
            if (c.getDni().equals(dni)) {
                return c;
            }
        }
        return null;
    }

    // Buscar cliente por código de mascota
    public Cliente buscarPorCodigoMascota(int codigoMascota) {
        for (Cliente c : clientes) {
            if (c.getCodigoMascota() == codigoMascota) {
                return c;
            }
        }
        return null;
    }

    // Tamaño del arreglo
    public int tamaño() {
        return clientes.size();
    }
}
