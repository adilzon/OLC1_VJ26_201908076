package olc1.golite.visitor.interpreter;

import java.util.List;
import olc1.golite.ast.stm.ParameterNode;
import olc1.golite.ast.ASTNode;

public class MethodSymbol extends FunctionSymbol {
    public final String receiverName;
    public final String receiverType;

    public MethodSymbol(String receiverName, String receiverType, String name, String returnType, List<ParameterNode> params, ASTNode body) {
        super(name, returnType, params, body);
        this.receiverName = receiverName;
        this.receiverType = receiverType;
    }
}
