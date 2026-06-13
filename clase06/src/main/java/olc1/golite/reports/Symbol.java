package olc1.golite.reports;

// Representa un registro en la Tabla de Símbolos.
// Guarda información esencial de cada variable: nombre, tipo, ámbito de vida y posición física.
public class Symbol {
    private final String name;
    private final String symbolType; // e.g., "Variable", "Método", "Función"
    private final String dataType;   // e.g., "int", "decimal", "string", "bool"
    private final String scope;      // Hierarchical scope representation (e.g. "Global -> If")
    private final int line;
    private final int column;

    public Symbol(String name, String symbolType, String dataType, String scope, int line, int column) {
        this.name = name;
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.scope = scope;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getScope() {
        return scope;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
