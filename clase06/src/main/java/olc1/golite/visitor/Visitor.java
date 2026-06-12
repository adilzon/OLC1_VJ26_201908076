package olc1.golite.visitor;

import olc1.golite.ast.exp.*;
import olc1.golite.ast.stm.*;

public interface Visitor<T> {
    T visit(Integers.Context ctx);
    T visit(Decimal.Context ctx);
    T visit(Paren.Context ctx);
    T visit(Add.Context ctx);
    T visit(Sub.Context ctx);
    T visit(Mul.Context ctx);
    T visit(Div.Context ctx);
    T visit(Negate.Context ctx);
    T visit(BoolLiteral.Context ctx);
    T visit(StringLiteral.Context ctx);
    T visit(VarRef.Context ctx);
    T visit(Imprimir.Context ctx);
    T visit(Assign.Context ctx);
    T visit(IfNode.Context ctx);
    T visit(Statments.Context ctx);
    T visit(ElifNode.Context ctx);
    T visit(ElifNodes.Context ctx);
    T visit(Allocate.Context ctx);
    T visit(Lt.Context ctx);
    T visit(Breakstm.Context ctx);
    T visit(WhileFor.Context ctx); 
}
