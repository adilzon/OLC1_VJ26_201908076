package olc1.golite.ast.stm;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class StructDeclarationNode implements ASTNode {
    public String name;
    public List<StructFieldNode> fields;

    public StructDeclarationNode(String name, List<StructFieldNode> fields) {
        this.name = name;
        this.fields = fields;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
