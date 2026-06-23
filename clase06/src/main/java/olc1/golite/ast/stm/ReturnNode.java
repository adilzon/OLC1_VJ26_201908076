package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class ReturnNode implements ASTNode {
    public ASTNode expression;   // puede ser null si es return vacío

    public ReturnNode(ASTNode expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
