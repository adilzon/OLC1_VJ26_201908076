package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class Lt implements ASTNode {
    // left < right
    private final ASTNode left, right;

    public Lt(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public static class Context {
        public final ASTNode left, right;

        public Context(Lt node) {
            this.left = node.left;
            this.right = node.right;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }   
}
