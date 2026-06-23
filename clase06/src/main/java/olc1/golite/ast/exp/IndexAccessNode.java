package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class IndexAccessNode implements ASTNode {
    public final ASTNode slice;
    public final ASTNode index;

    public IndexAccessNode(ASTNode slice, ASTNode index) {
        this.slice = slice;
        this.index = index;
    }

    public static class Context {
        public final ASTNode slice;
        public final ASTNode index;

        public Context(IndexAccessNode node) {
            this.slice = node.slice;
            this.index = node.index;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
