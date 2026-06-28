package olc1.golite.visitor;

import olc1.golite.ast.exp.*;
import olc1.golite.ast.stm.*;

/**
 * Visitor que transforma el AST completo en una cadena JSON anidada.
 * El JSON resultante tiene el formato { "name": "...", "children": [...] }
 * que es consumido directamente por el reporte HTML interactivo.
 */
public class AstJsonVisitor implements Visitor<String> {

    // ------------------------------------------------------------------ util
    private String node(String label, String... children) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"").append(esc(label)).append("\"");
        boolean hasChildren = false;
        for (String c : children) {
            if (c != null && !c.isEmpty()) {
                if (!hasChildren) {
                    sb.append(",\"children\":[");
                    hasChildren = true;
                } else {
                    sb.append(",");
                }
                sb.append(c);
            }
        }
        if (hasChildren) sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String leaf(String label) {
        return "{\"name\":\"" + esc(label) + "\"}";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    // ----------------------------------------------------------------- atoms
    @Override public String visit(Integers.Context ctx) { return leaf("Int: " + ctx.value); }
    @Override public String visit(Decimal.Context ctx)  { return leaf("Float64: " + ctx.value); }
    @Override public String visit(BoolLiteral.Context ctx) { return leaf("Bool: " + ctx.value); }
    @Override public String visit(StringLiteral.Context ctx) { return leaf("String: " + esc(ctx.value)); }
    @Override public String visit(RuneLiteral.Context ctx)   { return leaf("Rune: " + esc(ctx.value)); }
    @Override public String visit(NilLiteral.Context ctx)    { return leaf("nil"); }
    @Override public String visit(VarRef.Context ctx)        { return leaf("VarRef: " + ctx.name); }

    // --------------------------------------------------------------- arith
    @Override public String visit(Add.Context ctx)    { return node("+", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Sub.Context ctx)    { return node("-", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Mul.Context ctx)    { return node("*", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Div.Context ctx)    { return node("/", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Mod.Context ctx)    { return node("%", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Negate.Context ctx) { return node("Negate(-)", visit(ctx.expression)); }
    @Override public String visit(Paren.Context ctx)  { return node("(  )", visit(ctx.expression)); }

    // -------------------------------------------------------------- compare
    @Override public String visit(Lt.Context ctx)       { return node("<",  visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Gt.Context ctx)       { return node(">",  visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Lte.Context ctx)      { return node("<=", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Gte.Context ctx)      { return node(">=", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Equal.Context ctx)    { return node("==", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(NotEqual.Context ctx) { return node("!=", visit(ctx.left), visit(ctx.right)); }

    // ---------------------------------------------------------------- logic
    @Override public String visit(And.Context ctx) { return node("&&", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Or.Context ctx)  { return node("||", visit(ctx.left), visit(ctx.right)); }
    @Override public String visit(Not.Context ctx) { return node("!",  visit(ctx.expression)); }

    // ------------------------------------------------------------------ stm
    @Override
    public String visit(Statments.Context ctx) {
        if (ctx.statements == null || ctx.statements.isEmpty()) return leaf("Statments (vacío)");
        String[] kids = new String[ctx.statements.size()];
        for (int i = 0; i < ctx.statements.size(); i++) {
            kids[i] = ctx.statements.get(i).accept(this);
        }
        return node("Statments", kids);
    }

    @Override
    public String visit(Assign.Context ctx) {
        String typeInfo = (ctx.declaredType != null) ? " [" + ctx.declaredType + "]" : "";
        return node("Assign :=" + typeInfo, leaf("ID: " + ctx.name), visit(ctx.value));
    }

    @Override
    public String visit(Allocate.Context ctx) {
        return node("Assign =", leaf("ID: " + ctx.id), visit(ctx.expression));
    }

    @Override
    public String visit(Imprimir.Context ctx) {
        String[] kids = new String[ctx.expressions.size()];
        for (int i = 0; i < ctx.expressions.size(); i++) {
            kids[i] = ctx.expressions.get(i).accept(this);
        }
        return node("fmt.Println", kids);
    }

    @Override
    public String visit(IfNode.Context ctx) {
        String cond = visit(ctx.condition);
        String body = ctx.body != null ? ctx.body.accept(this) : leaf("(sin cuerpo)");
        String elifs = ctx.elifList != null ? ctx.elifList.accept(this) : null;
        return node("If", node("Condición", cond), node("Cuerpo", body),
                elifs != null ? node("ElseIf / Else", elifs) : null);
    }

    @Override
    public String visit(ElifNode.Context ctx) {
        String cond = visit(ctx.condition);
        String body = ctx.body != null ? ctx.body.accept(this) : leaf("(sin cuerpo)");
        return node("ElseIf", node("Condición", cond), node("Cuerpo", body));
    }

    @Override
    public String visit(ElifNodes.Context ctx) {
        if (ctx.elifNodesList == null || ctx.elifNodesList.isEmpty()) return leaf("ElifNodes (vacío)");
        String[] kids = new String[ctx.elifNodesList.size()];
        for (int i = 0; i < ctx.elifNodesList.size(); i++) {
            kids[i] = ctx.elifNodesList.get(i).accept(this);
        }
        return node("ElifNodes", kids);
    }

    @Override
    public String visit(WhileFor.Context ctx) {
        String cond = visit(ctx.condition);
        String body = ctx.body != null ? ctx.body.accept(this) : leaf("(sin cuerpo)");
        return node("For (while)", node("Condición", cond), node("Cuerpo", body));
    }

    @Override
    public String visit(ForClasico.Context ctx) {
        String init  = ctx.init  != null ? ctx.init.accept(this)  : leaf("(sin init)");
        String cond  = visit(ctx.condition);
        String post  = ctx.post  != null ? ctx.post.accept(this)  : leaf("(sin post)");
        String body  = ctx.body  != null ? ctx.body.accept(this)  : leaf("(sin cuerpo)");
        return node("For (clásico)", node("Init", init), node("Condición", cond),
                node("Post", post), node("Cuerpo", body));
    }

    @Override public String visit(Breakstm.Context ctx)    { return leaf("break"); }
    @Override public String visit(Continuestm.Context ctx) { return leaf("continue"); }
    @Override public String visit(ReturnStm.Context ctx)   {
        return ctx.expresion != null
                ? node("return", visit(ctx.expresion))
                : leaf("return");
    }
    @Override public String visit(ReturnNode.Context ctx) {
        return ctx.expression != null
                ? node("return", ctx.expression.accept(this))
                : leaf("return");
    }

    @Override
    public String visit(BlockStm.Context ctx) {
        return ctx.body != null ? node("Block", ctx.body.accept(this)) : leaf("Block (vacío)");
    }

    @Override
    public String visit(SwitchNode.Context ctx) {
        String expr = visit(ctx.expresion);
        String[] kids = new String[ctx.casos != null ? ctx.casos.size() + 1 : 1];
        kids[0] = node("Expresión", expr);
        if (ctx.casos != null) {
            for (int i = 0; i < ctx.casos.size(); i++) {
                kids[i + 1] = ctx.casos.get(i).accept(this);
            }
        }
        return node("Switch", kids);
    }

    @Override
    public String visit(CaseNode.Context ctx) {
        String cond = ctx.condicion != null ? node("Valor", visit(ctx.condicion)) : leaf("default");
        String body = ctx.instrucciones != null ? node("Cuerpo", ctx.instrucciones.accept(this)) : null;
        return node("Case", cond, body);
    }

    // ------------------------------------------------------------ functions
    @Override
    public String visit(FunctionDecl.Context ctx) {
        return node("FunctionDecl: " + ctx.id,
                ctx.instrucciones != null ? node("Cuerpo", ctx.instrucciones.accept(this)) : leaf("(sin cuerpo)"));
    }

    @Override
    public String visit(FunctionDeclarationNode.Context ctx) {
        String params = buildParamsList(ctx.parameters);
        String retType = leaf("ReturnType: " + (ctx.returnType == null || ctx.returnType.isEmpty() ? "void" : ctx.returnType));
        String body = ctx.body != null ? node("Cuerpo", ctx.body.accept(this)) : leaf("(sin cuerpo)");
        return node("FuncDecl: " + ctx.name, params, retType, body);
    }

    @Override
    public String visit(FunctionCall.Context ctx) {
        String[] kids = new String[ctx.argumentos.size()];
        for (int i = 0; i < ctx.argumentos.size(); i++) {
            kids[i] = node("Arg " + (i + 1), ctx.argumentos.get(i).accept(this));
        }
        return node("FuncCall: " + ctx.id, kids);
    }

    @Override
    public String visit(MethodDeclarationNode.Context ctx) {
        String params = buildParamsList(ctx.parameters);
        String retType = leaf("ReturnType: " + (ctx.returnType == null || ctx.returnType.isEmpty() ? "void" : ctx.returnType));
        String body = ctx.body != null ? node("Cuerpo", ctx.body.accept(this)) : leaf("(sin cuerpo)");
        return node("MethodDecl: " + ctx.receiverType + "." + ctx.name,
                leaf("Receptor: " + ctx.receiverName + " " + ctx.receiverType),
                params, retType, body);
    }

    @Override
    public String visit(MethodCallNode.Context ctx) {
        String[] kids = new String[ctx.arguments.size()];
        for (int i = 0; i < ctx.arguments.size(); i++) {
            kids[i] = node("Arg " + (i + 1), ctx.arguments.get(i).accept(this));
        }
        String target = node("Receptor", ctx.target.accept(this));
        String argsNode = node("Args", kids);
        return node("MethodCall: " + ctx.methodName, target, argsNode);
    }

    private String buildParamsList(java.util.List<ParameterNode> parameters) {
        if (parameters == null || parameters.isEmpty()) return leaf("Params: (ninguno)");
        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"Params\",\"children\":[");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(",");
            ParameterNode p = parameters.get(i);
            sb.append(leaf(p.name + ": " + p.type));
        }
        sb.append("]}");
        return sb.toString();
    }

    // ---------------------------------------------------------------- slices
    @Override
    public String visit(SliceLiteralNode.Context ctx) {
        if (ctx.elements == null || ctx.elements.isEmpty()) return leaf("Slice[] (vacío)");
        String[] kids = new String[ctx.elements.size()];
        for (int i = 0; i < ctx.elements.size(); i++) {
            kids[i] = ctx.elements.get(i).accept(this);
        }
        return node("Slice[" + ctx.elementType + "]", kids);
    }

    @Override
    public String visit(IndexAccessNode.Context ctx) {
        return node("IndexAccess", visit(ctx.slice), node("Index", visit(ctx.index)));
    }

    @Override
    public String visit(IndexAssignNode.Context ctx) {
        return node("IndexAssign", visit(ctx.slice), node("Index", visit(ctx.index)), node("Valor", visit(ctx.value)));
    }

    @Override
    public String visit(AppendNode.Context ctx) {
        return node("append", visit(ctx.slice), visit(ctx.element));
    }

    @Override
    public String visit(LenNode.Context ctx) {
        return node("len", visit(ctx.expression));
    }

    // --------------------------------------------------------------- structs
    @Override
    public String visit(StructDeclarationNode.Context ctx) {
        if (ctx.fields == null || ctx.fields.isEmpty()) return leaf("Struct: " + ctx.name + " {}");
        String[] kids = new String[ctx.fields.size()];
        for (int i = 0; i < ctx.fields.size(); i++) {
            StructFieldNode f = ctx.fields.get(i);
            kids[i] = leaf("Field: " + f.name + " " + f.type);
        }
        return node("Struct: " + ctx.name, kids);
    }

    @Override
    public String visit(StructLiteralNode.Context ctx) {
        String[] kids = new String[ctx.fieldNames.size()];
        for (int i = 0; i < ctx.fieldNames.size(); i++) {
            kids[i] = node(ctx.fieldNames.get(i) + ":", ctx.values.get(i).accept(this));
        }
        return node("StructLit: " + ctx.structName, kids);
    }

    @Override
    public String visit(FieldAccessNode.Context ctx) {
        return node("FieldAccess: ." + ctx.fieldName, visit(ctx.structExpr));
    }

    @Override
    public String visit(FieldAssignNode.Context ctx) {
        return node("FieldAssign: ." + ctx.fieldName, visit(ctx.structExpr), node("Valor", visit(ctx.value)));
    }

    // ------------------------------------------------------------------ misc
    @Override public String visit(Atoi.Context ctx)       { return node("strconv.Atoi", visit(ctx.expression)); }
    @Override public String visit(ParseFloat.Context ctx) { return node("strconv.ParseFloat", visit(ctx.expression)); }
    @Override public String visit(TypeOf.Context ctx)     { return node("typeof", visit(ctx.expression)); }
    @Override public String visit(TypeCast.Context ctx)   { return leaf("TypeCast"); }

    // ---------------------------------------------------------------- helper: dispatch
    private String visit(olc1.golite.ast.ASTNode node) {
        return node != null ? node.accept(this) : leaf("null");
    }
}
