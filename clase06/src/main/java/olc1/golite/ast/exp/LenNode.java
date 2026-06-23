package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class LenNode implements ASTNode {
    public ASTNode expression;

    public LenNode(ASTNode expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
