package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Nodo para la operación relacional de desigualdad (!=).
// Compara dos operandos del mismo tipo para asegurar que no sean iguales.
public class NotEqual implements ASTNode {
    private final ASTNode left, right;

    public NotEqual(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public static class Context {
        public final ASTNode left, right;

        public Context(NotEqual node) {
            this.left = node.left;
            this.right = node.right;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }   
}
