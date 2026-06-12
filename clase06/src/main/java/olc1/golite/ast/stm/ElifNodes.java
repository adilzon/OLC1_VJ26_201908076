package olc1.golite.ast.stm;

import java.util.ArrayList;
import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class ElifNodes implements ASTNode{
    // lista de elifs: [ElifNode, ElifNode, ...]
    private final java.util.List<ElifNode> elifNodesList;
    public Context ctx;

    public ElifNodes(ElifNode first) {
        this.elifNodesList = new ArrayList<>();
        this.elifNodesList.add(first);
        this.ctx = null;
    }

    public void add(ElifNode node) {
        this.elifNodesList.add(node);
    }

    public class Context {
        public final List<ElifNode> elifNodesList;

        public Context(ElifNodes node) {
            this.elifNodesList = node.elifNodesList;
            node.ctx = this;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}