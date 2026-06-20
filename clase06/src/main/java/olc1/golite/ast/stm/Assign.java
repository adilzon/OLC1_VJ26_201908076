package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Representa la declaración de variables con inferencia de tipos (:=) o var explícito.
// Introduce nuevas variables en el entorno actual del compilador.
public class Assign implements ASTNode {
    private final String name;
    private final ASTNode value;
    private final String declaredType;
    private final int line;
    private final int column;

    public Assign(String name, ASTNode value, String declaredType, int line, int column) {
        this.name = name;
        this.value = value;
        this.declaredType = declaredType;
        this.line = line;
        this.column = column;
    }

    public Assign(String name, ASTNode value, int line, int column) {
        this(name, value, null, line, column);
    }

    public static class Context {
        public final String name;
        public final ASTNode value;
        public final String declaredType;
        public final int line;
        public final int column;

        public Context(Assign node) {
            this.name = node.name;
            this.value = node.value;
            this.declaredType = node.declaredType;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
