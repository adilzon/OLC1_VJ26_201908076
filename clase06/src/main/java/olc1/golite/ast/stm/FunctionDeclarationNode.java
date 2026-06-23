package olc1.golite.ast.stm;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class FunctionDeclarationNode implements ASTNode {
    public String name;
    public List<ParameterNode> parameters;
    public String returnType;        // "int", "float64", "string", "bool", "void", etc.
    public ASTNode body;

    public FunctionDeclarationNode(String name, List<ParameterNode> params, String returnType, ASTNode body) {
        this.name = name;
        this.parameters = params;
        this.returnType = returnType;
        this.body = body;
    }

    public static class Context {
        public final String name;
        public final List<ParameterNode> parameters;
        public final String returnType;
        public final ASTNode body;

        public Context(FunctionDeclarationNode node) {
            this.name = node.name;
            this.parameters = node.parameters;
            this.returnType = node.returnType;
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
