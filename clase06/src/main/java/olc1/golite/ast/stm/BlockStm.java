package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Decidí implementar este nodo BlockStm para dar soporte a los bloques de sentencias delimitados por llaves {}.
// Esto me permite manejar de forma aislada e independiente los ámbitos y el shadowing de variables locales.
public class BlockStm implements ASTNode {
    private final Statments body;

    // Constructor donde almaceno las instrucciones pertenecientes al cuerpo del bloque.
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
