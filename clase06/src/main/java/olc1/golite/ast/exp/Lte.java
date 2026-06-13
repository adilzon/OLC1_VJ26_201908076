package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Nodo para la operación relacional menor o igual que (<=).
// Me permite comparar números o cadenas y determinar su orden relativo.
public class Lte implements ASTNode {
    private final ASTNode left, right;

    public Lte(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public static class Context {
        public final ASTNode left, right;

        public Context(Lte node) {
            this.left = node.left;
            this.right = node.right;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }   
}
