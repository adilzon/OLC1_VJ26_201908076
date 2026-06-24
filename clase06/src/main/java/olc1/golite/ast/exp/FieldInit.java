package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;

public class FieldInit {
    public final String name;
    public final ASTNode value;

    public FieldInit(String name, ASTNode value) {
        this.name = name;
        this.value = value;
    }
}
