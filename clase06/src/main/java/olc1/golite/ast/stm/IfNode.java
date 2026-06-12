package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class IfNode implements ASTNode {
    private final ASTNode condition;
    private final ASTNode body;
    private final ElifNodes elifList; 

    public IfNode(ASTNode condition, ASTNode body) {
        //if condition { body }
        this.condition = condition;
        this.body = body;
        this.elifList = null; 
    }

        public IfNode(ASTNode condition, ASTNode body, ElifNodes elifList) {
        //if condition { body } elif condition { body } elif condition { body } ...
        this.condition = condition;
        this.body = body;
        this.elifList = elifList; 
    }

    public static class Context {
        public final ASTNode condition;
        public final ASTNode body;
        public final ElifNodes elifList;
        public Context(IfNode node) {
            this.condition = node.condition;
            this.body = node.body;
            this.elifList = node.elifList;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
