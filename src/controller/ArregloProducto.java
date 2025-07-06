package controller;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import model.Producto;

public class ArregloProducto {
    
    private ArrayList<Producto> productos;
    private final int STOCK_MINIMO = 5;

    public ArregloProducto() {
        productos = new ArrayList<>();
    }

    public void adicionar(Producto p) {
        productos.add(p);
    }

    public int tamaño() {
        return productos.size();
    }

    public Producto obtener(int i) {
        return productos.get(i);
    }

    public Producto buscarPorNombre(String nombre) {
        for (Producto p : productos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return p;
            }
        }
        return null;
    }

    public boolean registrarVenta(String nombre, int cantidadVendida) {
        Producto p = buscarPorNombre(nombre);
        if (p != null) {
            if (p.getStock() >= cantidadVendida) {
                p.setStock(p.getStock() - cantidadVendida);
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Stock insuficiente para vender " + cantidadVendida + " unidades de " + nombre +
                        "\nStock disponible: " + p.getStock(),
                        "Error de Stock",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Producto no encontrado: " + nombre,
                    "Producto inexistente",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public ArrayList<Producto> productosConStockBajo() {
        ArrayList<Producto> alerta = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getStock() < STOCK_MINIMO) {
                alerta.add(p);
            }
        }
        return alerta;
    }

    public void eliminar(Producto p) {
        productos.remove(p);
    }
}
