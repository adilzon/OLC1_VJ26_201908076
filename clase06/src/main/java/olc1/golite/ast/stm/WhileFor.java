package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class WhileFor implements ASTNode{
    // For condition { body }
    private final ASTNode condition;
    private final ASTNode body; // statements   

    public WhileFor(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }   

    public static class Context {
        public final ASTNode condition;
        public final ASTNode body;

        public Context(WhileFor node) {
            this.condition = node.condition;
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {   
        return visitor.visit(new Context(this));
    }   
}
