package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class ReturnNode implements ASTNode {
    public ASTNode expression;   // puede ser null si es return vacío

    public ReturnNode(ASTNode expression) {
        this.expression = expression;
    }

    public static class Context {
        public final ASTNode expression;
        public Context(ReturnNode node) {
            this.expression = node.expression;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
