package olc1.golite.visitor.interpreter;

import java.util.List;
import olc1.golite.ast.stm.ParameterNode;
import olc1.golite.ast.ASTNode;

public class FunctionSymbol {
    public String name;
    public String returnType;
    public List<ParameterNode> parameters;
    public Enviroment localScope;   // Para variables locales dentro de la función
    public ASTNode body;

    public FunctionSymbol(String name, String returnType, List<ParameterNode> params, ASTNode body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = params;
        this.body = body;
        this.localScope = new Enviroment();
    }
}
