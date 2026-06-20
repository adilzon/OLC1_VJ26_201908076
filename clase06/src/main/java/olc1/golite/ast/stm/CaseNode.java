package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class CaseNode implements ASTNode {
    private final ASTNode condicion;
    private final ASTNode instrucciones;

    public CaseNode(ASTNode condicion, ASTNode instrucciones) {
        this.condicion = condicion;
        this.instrucciones = instrucciones;
    }

    public static class Context {
        public final ASTNode condicion;
        public final ASTNode instrucciones;
        public Context(CaseNode node) {
            this.condicion = node.condicion;
            this.instrucciones = node.instrucciones;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
