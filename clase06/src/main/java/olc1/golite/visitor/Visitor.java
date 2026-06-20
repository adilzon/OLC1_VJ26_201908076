package olc1.golite.visitor;

import olc1.golite.ast.exp.*;
import olc1.golite.ast.stm.*;

// Interfaz base del patrón Visitor.
// Define la firma de los métodos 'visit' para cada nodo concreto de mi estructura AST.
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
    T visit(Gt.Context ctx);
    T visit(Gte.Context ctx);
    T visit(Lte.Context ctx);
    T visit(Equal.Context ctx);
    T visit(NotEqual.Context ctx);
    T visit(Mod.Context ctx);
    T visit(And.Context ctx);
    T visit(Or.Context ctx);
    T visit(Not.Context ctx);
    T visit(Continuestm.Context ctx);
    T visit(Atoi.Context ctx);
    T visit(ParseFloat.Context ctx);
    T visit(TypeOf.Context ctx);
    T visit(BlockStm.Context ctx);
    T visit(ForClasico.Context ctx);
    T visit(NilLiteral.Context ctx);
    T visit(RuneLiteral.Context ctx);
    T visit(SwitchNode.Context ctx);
    T visit(CaseNode.Context ctx);
    T visit(ReturnStm.Context ctx);
    T visit(FunctionDecl.Context ctx);
    T visit(FunctionCall.Context ctx);
    T visit(TypeCast.Context ctx);
}
