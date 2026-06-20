package olc1.golite.ast.stm;

import java.util.List;
import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class FunctionDecl implements ASTNode {
    private final String id;
    private final List<Param> parametros;
    private final String tipoRetorno;
    private final ASTNode instrucciones;
    private final int line;
    private final int column;

    public FunctionDecl(String id, List<Param> parametros, String tipoRetorno, ASTNode instrucciones, int line, int column) {
        this.id = id;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.instrucciones = instrucciones;
        this.line = line;
        this.column = column;
    }

    public static class Context {
        public final String id;
        public final List<Param> parametros;
        public final String tipoRetorno;
        public final ASTNode instrucciones;
        public final int line;
        public final int column;
        public Context(FunctionDecl node) {
            this.id = node.id;
            this.parametros = node.parametros;
            this.tipoRetorno = node.tipoRetorno;
            this.instrucciones = node.instrucciones;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
