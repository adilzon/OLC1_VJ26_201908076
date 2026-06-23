package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class ParameterNode implements ASTNode {
    public String name;
    public String type;

    public ParameterNode(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
