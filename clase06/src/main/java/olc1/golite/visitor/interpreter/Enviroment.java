package olc1.golite.visitor.interpreter;

import java.util.HashMap;
import java.util.Map;

import olc1.golite.visitor.interpreter.value.ValueWrapper;

public class Enviroment {
    private final Map<String, ValueWrapper> variables;
    private final Enviroment parent;

    public Enviroment() {
        variables = new HashMap<>();
        this.parent = null;
    }

    public Enviroment(Enviroment parent) {
        variables = new HashMap<>();
        this.parent = parent;
    }

    public ValueWrapper declare(String name, ValueWrapper value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Variable " + name + " ya declarada en este ámbito");
        }
        variables.put(name, value);
        return value;
    }

    public ValueWrapper get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            throw new RuntimeException("Variable '" + name + "' no declarada");
        }
    }

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