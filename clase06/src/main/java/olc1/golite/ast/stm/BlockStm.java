package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class BlockStm implements ASTNode {
    private final Statments body;

    public BlockStm(Statments body) {
        this.body = body;
    }

    public static class Context {
        public final Statments body;

        public Context(BlockStm node) {
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
