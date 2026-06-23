package olc1.golite.visitor.interpreter;

import java.util.List;
import olc1.golite.ast.stm.ParameterNode;

public class FunctionSymbol {
    public String name;
    public String returnType;
    public List<ParameterNode> parameters;
    public Enviroment localScope;   // Para variables locales dentro de la función

    public FunctionSymbol(String name, String returnType, List<ParameterNode> params) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = params;
        this.localScope = new Enviroment();
    }
}
