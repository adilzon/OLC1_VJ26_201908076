package olc1.golite.visitor.interpreter;

import java.util.HashMap;
import java.util.Map;

import olc1.golite.visitor.interpreter.value.ValueWrapper;

// Con esta clase controlo la jerarquía de ámbitos (scopes). Me permite enlazar
// cada entorno local con su respectivo entorno padre, posibilitando herencia y shadowing.
public class Enviroment {
    private final Map<String, ValueWrapper> variables;
    private final Enviroment parent;
    private final String name;

    // Constructor para el ámbito raíz, el cual no tiene un entorno padre.
    public Enviroment() {
        variables = new HashMap<>();
        this.parent = null;
        this.name = "Global";
    }

    // Constructor genérico para sub-ámbitos locales.
    public Enviroment(Enviroment parent) {
        variables = new HashMap<>();
        this.parent = parent;
        this.name = "Local";
    }

    // Constructor que me permite identificar el tipo de ámbito para depuración y reportes.
    public Enviroment(Enviroment parent, String name) {
        variables = new HashMap<>();
        this.parent = parent;
        this.name = name;
    }

    // Genero una ruta legible del ámbito (ej: Global -> For -> Block)
    // que me sirve para estructurar los reportes de la tabla de símbolos.
    public String getScopeName() {
        if (parent == null) {
            return name;
        } else {
            return parent.getScopeName() + " -> " + name;
        }
    }

    // Registro una nueva variable únicamente en el nivel actual del entorno.
    // Si ya existe en este nivel específico, lanzo una excepción para evitar re-declaraciones.
    public ValueWrapper declare(String name, ValueWrapper value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Variable " + name + " ya declarada en este ámbito");
        }
        variables.put(name, value);
        return value;
    }

    // Obtengo el valor de una variable realizando una búsqueda recursiva hacia arriba
    // (del hijo hacia los padres) en la jerarquía de entornos.
    public ValueWrapper get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            throw new RuntimeException("Variable '" + name + "' no declarada");
        }
    }

    // Asigno un nuevo valor a una variable existente. Busco recursivamente hacia arriba
    // para encontrar en qué nivel fue declarada y actualizarla allí.
    public ValueWrapper set(String name, ValueWrapper value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
            return value;
        } else if (parent != null) {
            return parent.set(name, value);
        } else {
            throw new RuntimeException("Variable '" + name + "' no declarada");
        }
    }

}