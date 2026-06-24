package olc1.golite.ast.stm;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class MethodDeclarationNode extends FunctionDeclarationNode {
    public final String receiverName;   // nombre del parámetro receptor (ej: p)
    public final String receiverType;   // tipo del struct (ej: Persona)

    public MethodDeclarationNode(String receiverName, String receiverType, 
                                 String methodName, List<ParameterNode> params, 
                                 String returnType, ASTNode body) {
        super(methodName, params, returnType, body);
        this.receiverName = receiverName;
        this.receiverType = receiverType;
    }

    public static class Context {
        public final String receiverName;
        public final String receiverType;
        public final String name;
        public final List<ParameterNode> parameters;
        public final String returnType;
        public final ASTNode body;

        public Context(MethodDeclarationNode node) {
            this.receiverName = node.receiverName;
            this.receiverType = node.receiverType;
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
