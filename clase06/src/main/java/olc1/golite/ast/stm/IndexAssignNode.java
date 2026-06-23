package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class IndexAssignNode implements ASTNode {
    public final ASTNode slice;
    public final ASTNode index;
    public final ASTNode value;

    public IndexAssignNode(ASTNode slice, ASTNode index, ASTNode value) {
        this.slice = slice;
        this.index = index;
        this.value = value;
    }

    public static class Context {
        public final ASTNode slice;
        public final ASTNode index;
        public final ASTNode value;

        public Context(IndexAssignNode node) {
            this.slice = node.slice;
            this.index = node.index;
            this.value = node.value;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
