package olc1.golite.ast.stm;

import java.util.ArrayList;
import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Nodo para la sentencia de impresión (imprimir o fmt.Println).
// Me permite escribir en la consola del intérprete las expresiones dadas separadas por espacios.
public class Imprimir implements ASTNode {
    private final List<ASTNode> expressions;

    public Imprimir(ASTNode expression) {
        this.expressions = new ArrayList<>();
        this.expressions.add(expression);
    }

    public Imprimir(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    public static class Context {
        public final List<ASTNode> expressions;

        public Context(Imprimir node) {
            this.expressions = node.expressions;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
