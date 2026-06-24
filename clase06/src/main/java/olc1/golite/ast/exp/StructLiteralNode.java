package olc1.golite.ast.exp;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class StructLiteralNode implements ASTNode {
    public final String structName;
    public final List<String> fieldNames;
    public final List<ASTNode> values;

    public StructLiteralNode(String structName, List<String> fieldNames, List<ASTNode> values) {
        this.structName = structName;
        this.fieldNames = fieldNames;
        this.values = values;
    }

    public static class Context {
        public final String structName;
        public final List<String> fieldNames;
        public final List<ASTNode> values;

        public Context(StructLiteralNode node) {
            this.structName = node.structName;
            this.fieldNames = node.fieldNames;
            this.values = node.values;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
