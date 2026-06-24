package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class FieldAssignNode implements ASTNode {
    public final ASTNode structExpr;
    public final String fieldName;
    public final ASTNode value;

    public FieldAssignNode(ASTNode structExpr, String fieldName, ASTNode value) {
        this.structExpr = structExpr;
        this.fieldName = fieldName;
        this.value = value;
    }

    public static class Context {
        public final ASTNode structExpr;
        public final String fieldName;
        public final ASTNode value;

        public Context(FieldAssignNode node) {
            this.structExpr = node.structExpr;
            this.fieldName = node.fieldName;
            this.value = node.value;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
