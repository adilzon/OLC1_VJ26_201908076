package olc1.golite.ast.exp;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class MethodCallNode implements ASTNode {
    public final ASTNode target;
    public final String methodName;
    public final List<ASTNode> arguments;
    public final int line;
    public final int column;

    public MethodCallNode(ASTNode target, String methodName, List<ASTNode> arguments, int line, int column) {
        this.target = target;
        this.methodName = methodName;
        this.arguments = arguments;
        this.line = line;
        this.column = column;
    }

    public static class Context {
        public final ASTNode target;
        public final String methodName;
        public final List<ASTNode> arguments;
        public final int line;
        public final int column;

        public Context(MethodCallNode node) {
            this.target = node.target;
            this.methodName = node.methodName;
            this.arguments = node.arguments;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
