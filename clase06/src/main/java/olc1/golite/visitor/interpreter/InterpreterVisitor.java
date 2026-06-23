package olc1.golite.visitor.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import olc1.golite.ast.ASTNode;
import olc1.golite.ast.exp.*;
import olc1.golite.ast.stm.*;
import olc1.golite.reports.GoliteError;
import olc1.golite.reports.Symbol;
import olc1.golite.visitor.Visitor;
import olc1.golite.visitor.interpreter.value.*;
import olc1.golite.visitor.interpreter.transfer.*;

// Decidí utilizar el patrón de diseño Visitor para recorrer el árbol de sintaxis abstracta (AST)
// de forma limpia, separando por completo la estructura sintáctica de la lógica de evaluación.
public class InterpreterVisitor implements Visitor<ValueWrapper> {
    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    private Enviroment environment = new Enviroment();
    public final List<GoliteError> errors = new ArrayList<>();
    public final List<Symbol> symbols = new ArrayList<>();

    public ValueWrapper Visit(ASTNode node) {

        return node.accept(this);
    }

    @Override
    public ValueWrapper visit(Integers.Context ctx) {
        return new IntValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Decimal.Context ctx) {
        return new DecimalValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Add.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        // int + int
        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new IntValue(li.value() + ri.value(), li.line(), li.column());
        }
        // decimal + decimal
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new DecimalValue(ld.value() + rd.value(), ld.line(), ld.column());
        }
        // mixed int and decimal -> decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new DecimalValue(li.value() + rd.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new DecimalValue(ld.value() + ri.value(), ld.line(), ld.column());
        }
        // string concatenation
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new StringValue("\"" + l.toString() + r.toString() + "\"", l.line(), l.column());
        }

        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para suma: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return left;
    }

    @Override
    public ValueWrapper visit(Sub.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        // int - int
        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new IntValue(li.value() - ri.value(), li.line(), li.column());
        }
        // decimal - decimal
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new DecimalValue(ld.value() - rd.value(), ld.line(), ld.column());
        }
        // mixed int and decimal -> decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new DecimalValue(li.value() - rd.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new DecimalValue(ld.value() - ri.value(), ld.line(), ld.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para resta: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return left;
    }

    @Override
    public ValueWrapper visit(Mul.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        // int * int
        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new IntValue(li.value() * ri.value(), li.line(), li.column());
        }
        // decimal * decimal
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new DecimalValue(ld.value() * rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal -> decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new DecimalValue(li.value() * rd.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new DecimalValue(ld.value() * ri.value(), ld.line(), ri.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para multiplicacion: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return left;
    }

    @Override
    public ValueWrapper visit(Div.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        // int / int
        if (left instanceof IntValue li && right instanceof IntValue ri) {
            if (ri.value() == 0) {
                this.errors.add(new GoliteError("Semantico", "Division por cero", ri.line(), ri.column()));
                return new IntValue(0, li.line(), li.column());
            }
            return new IntValue(li.value() / ri.value(), li.line(), li.column());
        }
        // decimal / decimal
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            if (rd.value() == 0.0) {
                this.errors.add(new GoliteError("Semantico", "Division por cero", rd.line(), rd.column()));
                return new DecimalValue(0.0, ld.line(), ld.column());
            }
            return new DecimalValue(ld.value() / rd.value(), ld.line(), ld.column());
        }
        // mixed int and decimal -> decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            if (rd.value() == 0.0) {
                this.errors.add(new GoliteError("Semantico", "Division por cero", rd.line(), rd.column()));
                return new DecimalValue(0.0, li.line(), li.column());
            }
            return new DecimalValue(li.value() / rd.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            if (ri.value() == 0) {
                this.errors.add(new GoliteError("Semantico", "Division por cero", ri.line(), ri.column()));
                return new DecimalValue(0.0, ld.line(), ld.column());
            }
            return new DecimalValue(ld.value() / ri.value(), ld.line(), ld.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para division: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return left;
    }

    @Override
    public ValueWrapper visit(Lt.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new BoolValue(li.value() < ri.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new BoolValue(ld.value() < rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new BoolValue(li.value() < rd.value(), li.line(), rd.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new BoolValue(ld.value() < ri.value(), ld.line(), ri.column());
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new BoolValue(l.toString().compareTo(r.toString()) < 0, l.line(), l.column());
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return new BoolValue(l.value() < r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para comparacion <: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }


    @Override
    public ValueWrapper visit(Negate.Context ctx) {
        ValueWrapper operand = Visit(ctx.expression);
        return switch (operand) {
            case IntValue     v -> new IntValue(-v.value(), v.line(), v.column());
            case DecimalValue v -> new DecimalValue(-v.value(), v.line(), v.column());
            default -> {
                this.errors.add(new GoliteError("Semantico", "Operacion invalida: -" + operand.getTypeName(), operand.line(), operand.column()));
                yield operand;
            }
        };
    }

    @Override
    public ValueWrapper visit(Imprimir.Context ctx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ctx.expressions.size(); i++) {
            ValueWrapper val = Visit(ctx.expressions.get(i));
            sb.append(val.toString());
            if (i < ctx.expressions.size() - 1) {
                sb.append(" ");
            }
        }
        output += sb.toString() + "\n";
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Statments.Context ctx) {
        for (ASTNode statment : ctx.statements) {
            Visit(statment);
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Paren.Context ctx) {
        return Visit(ctx.expression);
    }

    @Override
    public ValueWrapper visit(BoolLiteral.Context ctx) {
        return new BoolValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(StringLiteral.Context ctx) {
        return new StringValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(VarRef.Context ctx) {
        try {
            return environment.get(ctx.name);
        } catch (RuntimeException e) {
            this.errors.add(
                new GoliteError(
                    "Semantico",
                    "Variable '" + ctx.name + "' no declarada",
                    ctx.line,
                    ctx.column));
            return defaultVoid;
        }        
    }

    @Override
    public ValueWrapper visit(Assign.Context ctx) {
        ValueWrapper val = Visit(ctx.value);
       
        try {
            // Declaro la variable en el entorno activo. 
            // Si la declaración es exitosa, la agrego a mi lista para la visualización del reporte de símbolos.
            environment.declare(ctx.name, val);
            boolean exists = false;
            String scopeName = environment.getScopeName();
            String typeName = val.getTypeName();
            if ("decimal".equals(typeName)) {
                typeName = "float64";
            }
            for (Symbol s : this.symbols) {
                if (s.getName().equals(ctx.name) && s.getScope().equals(scopeName) && s.getLine() == ctx.line && s.getColumn() == ctx.column) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                this.symbols.add(new Symbol(ctx.name, "Variable", typeName, scopeName, ctx.line, ctx.column));
            }
        } catch (RuntimeException e) {
            this.errors.add(
                new GoliteError(
                    "Semantico",
                    "Variable '" + ctx.name + "' ya declarada en este ámbito",
                    ctx.line,
                    ctx.column));
        }

        return defaultVoid;
    }    

    @Override
    public ValueWrapper visit(Allocate.Context ctx) {
        ValueWrapper val = Visit(ctx.expression);
       
        try {
            // Busco la variable en los entornos accesibles. Si ya existe,
            // valido estáticamente que el nuevo valor coincida con el tipo previamente definido.
            ValueWrapper existing = environment.get(ctx.id);
            if (existing != null && !existing.getTypeName().equals(val.getTypeName())) {
                this.errors.add(new GoliteError("Semantico", "Tipo incorrecto en asignacion a '" + ctx.id + "'. Se esperaba: " + existing.getTypeName() + ", obtenido: " + val.getTypeName(), ctx.line, ctx.column));
                return defaultVoid;
            }
            environment.set(ctx.id, val);
        } catch (RuntimeException e) {
            this.errors.add(
                new GoliteError(
                    "Semantico",
                    "Variable '" + ctx.id + "' no declarada",
                    ctx.line,
                    ctx.column));
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(IfNode.Context ctx) {
        Enviroment parentEnv = this.environment;
        
        // Evalúo la condición. Si no es booleana, registro el error semántico correspondiente.
        ValueWrapper cond = Visit(ctx.condition);
        if (!(cond instanceof BoolValue)) {
            this.errors.add(new GoliteError("Semantico", "La condicion de un 'if' debe evaluar a tipo bool, obtenido: " + cond.getTypeName(), cond.line(), cond.column()));
            return defaultVoid;
        }

        // Si la condición del if se cumple, creo un sub-ámbito y ejecuto su bloque.
        if (((BoolValue) cond).value()) {
            this.environment = new Enviroment(parentEnv, "If");
            Visit(ctx.body);
            this.environment = parentEnv;
            return defaultVoid;
        } 

        ElifNodes elifs = ctx.elifList;

        // Si la condición del if falló, evalúo los bloques Else If (elifs) y Else Puro secuencialmente.
        if (elifs != null) {
            Visit(elifs);

            for (ElifNode elif : elifs.ctx.elifNodesList) {
                Visit(elif);
                ValueWrapper elifCond = Visit(elif.ctx.condition);
                if (!(elifCond instanceof BoolValue)) {
                    this.errors.add(new GoliteError("Semantico", "La condicion de un 'else if' debe evaluar a tipo bool, obtenido: " + elifCond.getTypeName(), elifCond.line(), elifCond.column()));
                    continue;
                }

                if (((BoolValue) elifCond).value()) {
                    this.environment = new Enviroment(parentEnv, "Else If");
                    Visit(elif.ctx.body);
                    this.environment = parentEnv;
                    return defaultVoid;
                }
            }
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(ElifNodes.Context ctx) {
        return defaultVoid;
    }
    
    @Override
    public ValueWrapper visit(ElifNode.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(WhileFor.Context ctx) {
        Enviroment parentEnv = this.environment;
        ValueWrapper condition = Visit(ctx.condition);
        if (!(condition instanceof BoolValue)) {
            this.errors.add(new GoliteError("Semantico", "La condicion de un 'for' debe evaluar a tipo bool, obtenido: " + condition.getTypeName(), condition.line(), condition.column()));
            return defaultVoid;
        }

        // Bucle estilo while. Implementé excepciones personalizadas para controlar de forma
        // limpia las interrupciones del flujo que causan 'break' y 'continue'.
        while (condition instanceof BoolValue b && b.value()) {
            this.environment = new Enviroment(parentEnv, "For");

            try {
                Visit(ctx.body);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                // Ignoro la excepción del continue para saltar directamente a la siguiente condición
            }
           
            condition = Visit(ctx.condition);
            if (!(condition instanceof BoolValue)) {
                this.errors.add(new GoliteError("Semantico", "La condicion de un 'for' debe evaluar a tipo bool, obtenido: " + condition.getTypeName(), condition.line(), condition.column()));
                break;
            }
        }

        this.environment = parentEnv;
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Breakstm.Context ctx) {
        throw new BreakException();
    }

    @Override
    public ValueWrapper visit(Continuestm.Context ctx) {
        throw new ContinueException();
    }

    @Override
    public ValueWrapper visit(Gt.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new BoolValue(li.value() > ri.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new BoolValue(ld.value() > rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new BoolValue(li.value() > rd.value(), li.line(), rd.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new BoolValue(ld.value() > ri.value(), ld.line(), ri.column());
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new BoolValue(l.toString().compareTo(r.toString()) > 0, l.line(), l.column());
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return new BoolValue(l.value() > r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para comparacion >: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }


    @Override
    public ValueWrapper visit(Gte.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new BoolValue(li.value() >= ri.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new BoolValue(ld.value() >= rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new BoolValue(li.value() >= rd.value(), li.line(), rd.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new BoolValue(ld.value() >= ri.value(), ld.line(), ri.column());
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new BoolValue(l.toString().compareTo(r.toString()) >= 0, l.line(), l.column());
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return new BoolValue(l.value() >= r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para comparacion >=: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }


    @Override
    public ValueWrapper visit(Lte.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new BoolValue(li.value() <= ri.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new BoolValue(ld.value() <= rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new BoolValue(li.value() <= rd.value(), li.line(), rd.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new BoolValue(ld.value() <= ri.value(), ld.line(), ri.column());
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new BoolValue(l.toString().compareTo(r.toString()) <= 0, l.line(), l.column());
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return new BoolValue(l.value() <= r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para comparacion <=: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }


    @Override
    public ValueWrapper visit(Equal.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new BoolValue(li.value() == ri.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new BoolValue(ld.value() == rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new BoolValue(li.value() == rd.value(), li.line(), rd.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new BoolValue(ld.value() == ri.value(), ld.line(), ri.column());
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new BoolValue(l.value().equals(r.value()), l.line(), l.column());
        }
        if (left instanceof BoolValue l && right instanceof BoolValue r) {
            return new BoolValue(l.value() == r.value(), l.line(), l.column());
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return new BoolValue(l.value() == r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para comparacion ==: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }


    @Override
    public ValueWrapper visit(NotEqual.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return new BoolValue(li.value() != ri.value(), li.line(), li.column());
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return new BoolValue(ld.value() != rd.value(), ld.line(), rd.column());
        }
        // mixed int and decimal
        if (left instanceof IntValue li && right instanceof DecimalValue rd) {
            return new BoolValue(li.value() != rd.value(), li.line(), rd.column());
        }
        if (left instanceof DecimalValue ld && right instanceof IntValue ri) {
            return new BoolValue(ld.value() != ri.value(), ld.line(), ri.column());
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return new BoolValue(!l.value().equals(r.value()), l.line(), l.column());
        }
        if (left instanceof BoolValue l && right instanceof BoolValue r) {
            return new BoolValue(l.value() != r.value(), l.line(), l.column());
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return new BoolValue(l.value() != r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Tipos incompatibles para comparacion !=: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }


    @Override
    public ValueWrapper visit(Mod.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        // Operación de módulo (%). Verifico que ambos operandos sean enteros 
        // y que no haya división por cero.
        if (left instanceof IntValue l && right instanceof IntValue r) {
            if (r.value() == 0) {
                this.errors.add(new GoliteError("Semantico", "Division por cero en modulo", r.line(), r.column()));
                return new IntValue(0, l.line(), l.column());
            }
            return new IntValue(l.value() % r.value(), l.line(), l.column());
        }
        this.errors.add(new GoliteError("Semantico", "Operador % solo se permite para tipo int, obtenido: " + left.getTypeName() + " y " + right.getTypeName(), left.line(), left.column()));
        return left;
    }

    @Override
    public ValueWrapper visit(And.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        // Implementación de evaluación cortocircuitada para el operador lógico &&.
        if (left instanceof BoolValue l) {
            if (!l.value()) {
                return new BoolValue(false, l.line(), l.column());
            }
            ValueWrapper right = Visit(ctx.right);
            if (right instanceof BoolValue r) {
                return new BoolValue(r.value(), l.line(), l.column());
            }
            this.errors.add(new GoliteError("Semantico", "Operando derecho de && debe ser de tipo bool, obtenido: " + right.getTypeName(), left.line(), left.column()));
            return new BoolValue(false, left.line(), left.column());
        }
        this.errors.add(new GoliteError("Semantico", "Operando izquierdo de && debe ser de tipo bool, obtenido: " + left.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }

    @Override
    public ValueWrapper visit(Or.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        // Implementación de evaluación cortocircuitada para el operador lógico ||.
        if (left instanceof BoolValue l) {
            if (l.value()) {
                return new BoolValue(true, l.line(), l.column());
            }
            ValueWrapper right = Visit(ctx.right);
            if (right instanceof BoolValue r) {
                return new BoolValue(r.value(), l.line(), l.column());
            }
            this.errors.add(new GoliteError("Semantico", "Operando derecho de || debe ser de tipo bool, obtenido: " + right.getTypeName(), left.line(), left.column()));
            return new BoolValue(false, left.line(), left.column());
        }
        this.errors.add(new GoliteError("Semantico", "Operando izquierdo de || debe ser de tipo bool, obtenido: " + left.getTypeName(), left.line(), left.column()));
        return new BoolValue(false, left.line(), left.column());
    }

    @Override
    public ValueWrapper visit(Not.Context ctx) {
        ValueWrapper operand = Visit(ctx.expression);
        if (operand instanceof BoolValue v) {
            return new BoolValue(!v.value(), v.line(), v.column());
        }
        this.errors.add(new GoliteError("Semantico", "Operando de ! debe ser de tipo bool, obtenido: " + operand.getTypeName(), operand.line(), operand.column()));
        return new BoolValue(false, operand.line(), operand.column());
    }

    @Override
    public ValueWrapper visit(Atoi.Context ctx) {
        ValueWrapper val = Visit(ctx.expression);
        if (val instanceof StringValue s) {
            try {
                int parsed = Integer.parseInt(s.toString().trim());
                return new IntValue(parsed, ctx.line, ctx.column);
            } catch (NumberFormatException e) {
                this.errors.add(new GoliteError("Semantico", "No se pudo convertir string a int: " + s.toString(), ctx.line, ctx.column));
                return new IntValue(0, ctx.line, ctx.column);
            }
        }
        this.errors.add(new GoliteError("Semantico", "strconv.Atoi requiere un operando de tipo string, obtenido: " + val.getTypeName(), ctx.line, ctx.column));
        return new IntValue(0, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(ParseFloat.Context ctx) {
        ValueWrapper val = Visit(ctx.expression);
        if (val instanceof StringValue s) {
            try {
                double parsed = Double.parseDouble(s.toString().trim());
                return new DecimalValue(parsed, ctx.line, ctx.column);
            } catch (NumberFormatException e) {
                this.errors.add(new GoliteError("Semantico", "No se pudo convertir string a float64: " + s.toString(), ctx.line, ctx.column));
                return new DecimalValue(0.0, ctx.line, ctx.column);
            }
        }
        this.errors.add(new GoliteError("Semantico", "strconv.ParseFloat requiere un operando de tipo string, obtenido: " + val.getTypeName(), ctx.line, ctx.column));
        return new DecimalValue(0.0, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(TypeOf.Context ctx) {
        ValueWrapper val = Visit(ctx.expression);
        String typeName = val.getTypeName();
        if ("decimal".equalsIgnoreCase(typeName)) {
            typeName = "float64";
        }
        return new StringValue("\"" + typeName + "\"", ctx.line, ctx.column);
    }

    // Nil literal visitor
    @Override
    public ValueWrapper visit(NilLiteral.Context ctx) {
        return new NilValue(ctx.line, ctx.column);
    }

    // Rune literal visitor
    @Override
    public ValueWrapper visit(RuneLiteral.Context ctx) {
        String raw = ctx.value;
        if (raw.length() >= 2 && raw.charAt(0) == '\'' && raw.charAt(raw.length() - 1) == '\'') {
            raw = raw.substring(1, raw.length() - 1);
        }
        raw = raw.replace("\\\\", "\\").replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r");
        char ch = raw.isEmpty() ? '\0' : raw.charAt(0);
        return new RuneValue(ch, ctx.line, ctx.column);
    }


    @Override
    public ValueWrapper visit(BlockStm.Context ctx) {
        Enviroment parentEnv = this.environment;
        // Creo un nuevo entorno local para el bloque de llaves {}. 
        // Esto permite el anidamiento y shadowing de variables temporales.
        this.environment = new Enviroment(parentEnv, "Block");
        Visit(ctx.body);
        this.environment = parentEnv;
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(ForClasico.Context ctx) {
        Enviroment parentEnv = this.environment;
        // Inicializo el ámbito principal del For clásico (donde vive la variable de control 'init')
        this.environment = new Enviroment(parentEnv, "ForClasico");

        if (ctx.init != null) {
            Visit(ctx.init);
        }

        ValueWrapper condVal = Visit(ctx.condition);
        if (!(condVal instanceof BoolValue)) {
            this.errors.add(new GoliteError("Semantico", "La condicion de un 'for' clasico debe evaluar a tipo bool, obtenido: " + condVal.getTypeName(), condVal.line(), condVal.column()));
            this.environment = parentEnv;
            return defaultVoid;
        }

        // Bucle clásico de 3 partes. Creo un sub-entorno local para el cuerpo en cada iteración,
        // manejando 'break' y 'continue' de forma que continúe al bloque 'post' de actualización.
        while (((BoolValue) condVal).value()) {
            Enviroment bodyEnv = this.environment;
            this.environment = new Enviroment(bodyEnv, "ForBody");

            try {
                Visit(ctx.body);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                // El continue salta la iteración actual y va directo al bloque post
            } finally {
                this.environment = bodyEnv;
            }

            if (ctx.post != null) {
                Visit(ctx.post);
            }

            condVal = Visit(ctx.condition);
            if (!(condVal instanceof BoolValue)) {
                this.errors.add(new GoliteError("Semantico", "La condicion de un 'for' clasico debe evaluar a tipo bool, obtenido: " + condVal.getTypeName(), condVal.line(), condVal.column()));
                break;
            }
        }

        this.environment = parentEnv;
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(SwitchNode.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(CaseNode.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(ReturnStm.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(FunctionDecl.Context ctx) {
        if ("main".equals(ctx.id)) {
            if (ctx.instrucciones != null) {
                Visit(ctx.instrucciones);
            }
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(FunctionCall.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(TypeCast.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(FunctionDeclarationNode.Context ctx) {
        if ("main".equals(ctx.name)) {
            if (ctx.body != null) {
                Visit(ctx.body);
            }
        }
        return defaultVoid;
    }
}
