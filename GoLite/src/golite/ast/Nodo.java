package golite.ast;

import java.util.ArrayList;

public class Nodo {
    
    public String tipo;
    public Object valor;
    public ArrayList<Nodo> hijos;
    public int linea;
    public int columna;

    // Constructor básico con tipo y valor
    public Nodo(String tipo, Object valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    // Constructor con lista de hijos (para bloques y listas)
    public Nodo(String tipo, ArrayList hijos) {
        this.tipo = tipo;
        this.valor = null;
        this.hijos = hijos != null ? hijos : new ArrayList<>();
    }

    // Constructor para nodos con 2 hijos (operaciones binarias)
    public Nodo(String tipo, Object val1, Object val2) {
        this.tipo = tipo;
        this.valor = val1;
        this.hijos = new ArrayList<>();
        if (val2 instanceof Nodo) this.hijos.add((Nodo) val2);
        if (val2 instanceof ArrayList) this.hijos.addAll((ArrayList) val2);
    }

    // Constructor para nodos con 3 hijos (if-else, for, etc)
    public Nodo(String tipo, Object val1, Object val2, Object val3) {
        this.tipo = tipo;
        this.valor = val1;
        this.hijos = new ArrayList<>();
        agregarHijo(val2);
        agregarHijo(val3);
    }

    // Constructor para nodos con 4 hijos (for clásico, funciones)
    public Nodo(String tipo, Object val1, Object val2, Object val3, Object val4) {
        this.tipo = tipo;
        this.valor = val1;
        this.hijos = new ArrayList<>();
        agregarHijo(val2);
        agregarHijo(val3);
        agregarHijo(val4);
    }

    // Método auxiliar para agregar hijos
    private void agregarHijo(Object obj) {
        if (obj instanceof Nodo) {
            this.hijos.add((Nodo) obj);
        } else if (obj instanceof ArrayList) {
            // Para listas creamos un nodo contenedor
            Nodo contenedor = new Nodo("lista", (ArrayList) obj);
            this.hijos.add(contenedor);
        }
    }

    // Para debug, muestra el nodo como texto
    @Override
    public String toString() {
        return "Nodo[" + tipo + 
               (valor != null ? ", valor=" + valor : "") + 
               (!hijos.isEmpty() ? ", hijos=" + hijos.size() : "") + 
               "]";
    }
}