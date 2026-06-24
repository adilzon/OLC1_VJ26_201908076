package olc1.golite.ast.stm;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class StructDeclarationNode implements ASTNode {
    public final String name;
    public final List<StructFieldNode> fields;

    public StructDeclarationNode(String name, List<StructFieldNode> fields) {
        this.name = name;
        this.fields = fields;
    }

    public static class Context {
        public final String name;
        public final List<StructFieldNode> fields;

        public Context(StructDeclarationNode node) {
            this.name = node.name;
            this.fields = node.fields;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
