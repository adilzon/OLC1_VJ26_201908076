package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Nodo para el bucle clásico de tres partes (init; cond; post).
// Me permite iterar controlando el inicio, la condición y el incremento en su propio ámbito.
public class ForClasico implements ASTNode {
    private final ASTNode init;
    private final ASTNode condition;
    private final ASTNode post;
    private final ASTNode body;

    public ForClasico(ASTNode init, ASTNode condition, ASTNode post, ASTNode body) {
        this.init = init;
        this.condition = condition;
        this.post = post;
        this.body = body;
    }

    public static class Context {
        public final ASTNode init;
        public final ASTNode condition;
        public final ASTNode post;
        public final ASTNode body;

        public Context(ForClasico node) {
            this.init = node.init;
            this.condition = node.condition;
            this.post = node.post;
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
