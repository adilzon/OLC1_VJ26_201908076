package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class FieldAccessNode implements ASTNode {
    public final ASTNode structExpr;
    public final String fieldName;

    public FieldAccessNode(ASTNode structExpr, String fieldName) {
        this.structExpr = structExpr;
        this.fieldName = fieldName;
    }

    public static class Context {
        public final ASTNode structExpr;
        public final String fieldName;

        public Context(FieldAccessNode node) {
            this.structExpr = node.structExpr;
            this.fieldName = node.fieldName;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
