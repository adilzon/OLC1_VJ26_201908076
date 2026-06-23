package olc1.golite.visitor.interpreter;

import java.util.HashMap;
import java.util.Map;

public class StructSymbol {
    public String name;
    public Map<String, String> fields;   // nombreCampo -> tipo

    public StructSymbol(String name) {
        this.name = name;
        this.fields = new HashMap<>();
    }
}
