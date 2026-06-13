package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Nodo para la operación de residuo o módulo (%).
// Valida que los operandos sean enteros y calcula el residuo de la división.
public class Mod implements ASTNode {
    private final ASTNode left, right;

    public Mod(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public static class Context {
        public final ASTNode left, right;

        public Context(Mod node) {
            this.left = node.left;
            this.right = node.right;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }   
}
