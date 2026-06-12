package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class Allocate implements ASTNode {
    // id = EXPRESSION
    private final String id;
    private final ASTNode expression;
    private int line, column;

    public Allocate(String id, ASTNode expression, int line, int column) {
    this.id = id;
    this.expression = expression;
    this.line = line;
    this.column = column;
    }

    public static class Context {
        public final String id;
        public final ASTNode expression;
        public final int line, column;

        public Context(Allocate node) {
            this.id = node.id;
            this.expression = node.expression;
            this.line = node.line;
            this.column = node.column;
        }
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}