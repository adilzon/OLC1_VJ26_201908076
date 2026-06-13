package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Nodo individual para representar una rama Else If.
// Contiene su propia condición y el cuerpo de instrucciones correspondiente.
public class ElifNode implements ASTNode {
    // else if condition { statements }
    private final ASTNode condition;
    private final ASTNode body; // statements
    public Context ctx;

    public ElifNode(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }

    public static class Context {
        public final ASTNode condition;
        public final ASTNode body;

        public Context(ElifNode node) {
            this.condition = node.condition;
            this.body = node.body;
            node.ctx = this;
        }
    }    

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}