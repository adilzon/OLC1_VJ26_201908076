package olc1.golite.reports;

// Representa un lexema reconocido por el analizador léxico (Lexer).
// Contiene el tipo de token, el texto original escaneado y su posición en el código fuente.
public class Token {
    private final String type;
    private final String lexeme;
    private final int line;
    private final int column;

    public Token(String type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
