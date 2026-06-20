package olc1.golite.ast.stm;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class SwitchNode implements ASTNode {
    private final ASTNode expresion;
    private final List<ASTNode> casos;

    public SwitchNode(ASTNode expresion, List<ASTNode> casos) {
        this.expresion = expresion;
        this.casos = casos;
    }

    public static class Context {
        public final ASTNode expresion;
        public final List<ASTNode> casos;
        public Context(SwitchNode node) {
            this.expresion = node.expresion;
            this.casos = node.casos;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
