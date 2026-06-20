package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class TypeCast implements ASTNode {
    private final String tipoDestino;
    private final ASTNode expresion;
    private final int line;
    private final int column;

    public TypeCast(String tipoDestino, ASTNode expresion, int line, int column) {
        this.tipoDestino = tipoDestino;
        this.expresion = expresion;
        this.line = line;
        this.column = column;
    }

    public static class Context {
        public final String tipoDestino;
        public final ASTNode expresion;
        public final int line;
        public final int column;
        public Context(TypeCast node) {
            this.tipoDestino = node.tipoDestino;
            this.expresion = node.expresion;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
