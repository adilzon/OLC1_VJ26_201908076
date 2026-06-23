package olc1.golite.ast.exp;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class SliceLiteralNode implements ASTNode {
    public List<ASTNode> elements;

    public SliceLiteralNode(List<ASTNode> elements) {
        this.elements = elements;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
