package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class AppendNode implements ASTNode {
    public ASTNode slice;
    public ASTNode element;

    public AppendNode(ASTNode slice, ASTNode element) {
        this.slice = slice;
        this.element = element;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
