package olc1.golite.visitor.interpreter.transfer;

// Excepción personalizada para errores semánticos NO recuperables que deben detener la ejecución
// de un nodo y propagar el error hacia arriba del árbol AST.
// A diferencia de registrar el error en la lista y continuar, esta excepción
// interrumpe completamente la evaluación del sub-árbol actual.
public class SemanticException extends RuntimeException {
    private final int line;
    private final int column;

    public SemanticException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
