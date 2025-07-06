package controller;
import java.util.ArrayList;
import model.Empleado;

public class ArregloEmpleado {
    private ArrayList<Empleado> emple;

    // Constructor sin parámetros que crea el ArrayList
    public ArregloEmpleado() {
        emple = new ArrayList<Empleado>();
    }

    // Método para adicionar un empleado
    public void adicionar(Empleado x) {
        emple.add(x);
    }

    // Método para eliminar un empleado
    public void eliminar(Empleado x) {
        emple.remove(x);
    }

    // Obtener empleado por posición
    public Empleado obtener(int pos) {
        return emple.get(pos);
    }

    // Buscar empleado por código
    public Empleado buscar(int codigo) {
        for (Empleado e : emple) {
            if (e.getCodigo() == codigo) {
                return e;
            }
        }
        return null;
    }

    public int tamaño() {
        return emple.size();
    }
}
