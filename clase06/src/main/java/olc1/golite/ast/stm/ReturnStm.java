package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class ReturnStm implements ASTNode {
    private final ASTNode expresion;
    private final int line;
    private final int column;

    public ReturnStm(ASTNode expresion, int line, int column) {
        this.expresion = expresion;
        this.line = line;
        this.column = column;
    }

    public static class Context {
        public final ASTNode expresion;
        public final int line;
        public final int column;
        public Context(ReturnStm node) {
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
