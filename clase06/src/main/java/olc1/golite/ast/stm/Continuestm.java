package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class Continuestm implements ASTNode {
    private int line, column;

    public Continuestm(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public static class Context {
        public final int line, column;

        public Context(Continuestm node) {
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }   
}
