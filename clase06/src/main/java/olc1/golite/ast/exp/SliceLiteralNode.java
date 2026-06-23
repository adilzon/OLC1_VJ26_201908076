package olc1.golite.ast.exp;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class SliceLiteralNode implements ASTNode {
    public final List<ASTNode> elements;
    public final String elementType;

    public SliceLiteralNode(List<ASTNode> elements, String elementType) {
        this.elements = elements;
        this.elementType = elementType;
    }

    public static class Context {
        public final List<ASTNode> elements;
        public final String elementType;

        public Context(SliceLiteralNode node) {
            this.elements = node.elements;
            this.elementType = node.elementType;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
