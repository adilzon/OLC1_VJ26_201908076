package olc1.golite.ast.exp;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class FunctionCall implements ASTNode {
    private final String id;
    private final List<ASTNode> argumentos;
    private final int line;
    private final int column;

    public FunctionCall(String id, List<ASTNode> argumentos, int line, int column) {
        this.id = id;
        this.argumentos = argumentos;
        this.line = line;
        this.column = column;
    }

    public static class Context {
        public final String id;
        public final List<ASTNode> argumentos;
        public final int line;
        public final int column;
        public Context(FunctionCall node) {
            this.id = node.id;
            this.argumentos = node.argumentos;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
