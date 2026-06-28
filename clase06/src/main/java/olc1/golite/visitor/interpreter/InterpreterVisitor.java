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
import olc1.golite.visitor.interpreter.transfer.SemanticException;

// Decidí utilizar el patrón de diseño Visitor para recorrer el árbol de sintaxis abstracta (AST)
// de forma limpia, separando por completo la estructura sintáctica de la lógica de evaluación.
public class InterpreterVisitor implements Visitor<ValueWrapper> {
    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    private Enviroment environment = new Enviroment();
    public final List<GoliteError> errors = new ArrayList<>();
    public final List<Symbol> symbols = new ArrayList<>();
    private ValueWrapper returnValue = null;
    private boolean returning = false;

    public ValueWrapper Visit(ASTNode node) {
        // Captura cualquier SemanticException lanzada por los visitors hijos.
        // Esto permite registrar el error semántico y continuar con el siguiente nodo del AST
        // en lugar de colapsar toda la ejecución.
        try {
            return node.accept(this);
        } catch (SemanticException e) {
            this.errors.add(new GoliteError(
                "Semantico",
                e.getMessage(),
                e.getLine(),
                e.getColumn()));
            return defaultVoid;
        }
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
            if (this.returning) {
                break;
            }
            Visit(statment);
        }

        // Si estamos en el ámbito raíz (Global) y existe la función main, la ejecutamos después de procesar todo.
        if (this.environment.getParent() == null) {
            FunctionSymbol mainFunc = this.environment.lookupFunction("main");
            if (mainFunc != null) {
                Enviroment mainEnv = new Enviroment(this.environment, "Funcion main");
                Enviroment callerEnv = this.environment;
                this.environment = mainEnv;

                if (mainFunc.body != null) {
                    Visit(mainFunc.body);
                }

                this.environment = callerEnv;
            }
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
            
            if (this.returning) {
                break;
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

            if (this.returning) {
                break;
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
        ValueWrapper switchVal = Visit(ctx.expresion);
        
        CaseNode defaultCase = null;
        boolean matched = false;

        try {
            for (ASTNode node : ctx.casos) {
                if (this.returning) {
                    break;
                }
                if (node instanceof CaseNode caseNode) {
                    if (caseNode.getCondicion() == null) {
                        defaultCase = caseNode;
                        continue;
                    }

                    ValueWrapper caseVal = Visit(caseNode.getCondicion());
                    if (areEqual(switchVal, caseVal)) {
                        matched = true;
                        Visit(caseNode);
                        break;
                    }
                }
            }

            if (!matched && defaultCase != null && !this.returning) {
                Visit(defaultCase);
            }
        } catch (BreakException e) {
            // Un break dentro de un switch termina la ejecución del switch
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(CaseNode.Context ctx) {
        if (ctx.instrucciones != null) {
            Visit(ctx.instrucciones);
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(ReturnStm.Context ctx) {
        if (ctx.expresion != null) {
            this.returnValue = Visit(ctx.expresion);
        } else {
            this.returnValue = defaultVoid;
        }
        this.returning = true;
        return this.returnValue;
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
        // 1. Buscar la función en la tabla de símbolos (environment)
        FunctionSymbol func = environment.lookupFunction(ctx.id);
        
        if (func == null) {
            this.errors.add(new GoliteError("Semantico", "Funcion '" + ctx.id + "' no declarada", ctx.line, ctx.column));
            return defaultVoid;
        }

        // 2. Verificar cantidad de argumentos
        if (ctx.argumentos.size() != func.parameters.size()) {
            this.errors.add(new GoliteError("Semantico", "Cantidad incorrecta de argumentos en llamada a " + ctx.id + ". Se esperaban: " + func.parameters.size() + ", obtenido: " + ctx.argumentos.size(), ctx.line, ctx.column));
            return defaultVoid;
        }

        // 3. Evaluar los argumentos en el ámbito actual
        List<ValueWrapper> evaluatedArgs = new ArrayList<>();
        for (ASTNode arg : ctx.argumentos) {
            evaluatedArgs.add(Visit(arg));
        }

        // 4. Buscar el entorno global

        Enviroment globalEnv = this.environment;
        while (globalEnv.getParent() != null) {
            globalEnv = globalEnv.getParent();
        }

        // 5. Crear el nuevo entorno local para la llamada (lexical scoping)
        Enviroment funcEnv = new Enviroment(globalEnv, "Funcion " + ctx.id);

        // 6. Registrar parámetros como variables locales
        for (int i = 0; i < func.parameters.size(); i++) {
            ParameterNode param = func.parameters.get(i);
            ValueWrapper val = evaluatedArgs.get(i);
            funcEnv.declare(param.name, val);
            
            // Registrar en la lista de símbolos del reporte
            String typeName = val.getTypeName();
            if ("decimal".equals(typeName)) {
                typeName = "float64";
            }
            this.symbols.add(new Symbol(param.name, "Variable", typeName, funcEnv.getScopeName(), ctx.line, ctx.column));
        }

        // Guardar estado de retorno previo (para soportar recursión y llamadas anidadas)
        ValueWrapper oldReturnValue = this.returnValue;
        boolean oldReturning = this.returning;

        // Reiniciar variables de retorno para esta llamada
        this.returnValue = defaultVoid;
        this.returning = false;

        // 7. Guardar entorno del invocador y ejecutar cuerpo
        Enviroment callerEnv = this.environment;
        this.environment = funcEnv;

        // Protegemos la ejecucion del cuerpo de la funcion con try-catch para capturar
        // errores semanticos no recuperables que puedan surgir dentro del cuerpo.
        try {
            if (func.body != null) {
                Visit(func.body);
            }
        } catch (SemanticException e) {
            this.errors.add(new GoliteError(
                "Semantico",
                "Error en funcion '" + ctx.id + "': " + e.getMessage(),
                e.getLine(),
                e.getColumn()));
        } finally {
            this.environment = callerEnv;
        }

        // Capturar resultado e indicar que ya no estamos retornando en el llamador
        ValueWrapper resultValue = this.returnValue;

        // Restaurar estado de retorno previo
        this.returnValue = oldReturnValue;
        this.returning = oldReturning;

        return resultValue;
    }

    @Override
    public ValueWrapper visit(TypeCast.Context ctx) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(FunctionDeclarationNode.Context ctx) {
        if (environment.lookupFunction(ctx.name) != null) {
            this.errors.add(new GoliteError("Semantico", "Funcion '" + ctx.name + "' ya declarada", 1, 1));
        } else {
            environment.insertFunction(ctx.name, ctx.returnType, ctx.parameters, ctx.body);
            // Registrar la funcion en la tabla de simbolos para el reporte
            String returnType = (ctx.returnType == null || ctx.returnType.isEmpty()) ? "void" : ctx.returnType;
            String scopeName = environment.getScopeName();
            this.symbols.add(new Symbol(ctx.name, "Funcion", returnType, scopeName, -1, -1));
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(ReturnNode.Context ctx) {
        if (ctx.expression != null) {
            this.returnValue = Visit(ctx.expression);
        } else {
            this.returnValue = defaultVoid;
        }
        this.returning = true;
        return this.returnValue;
    }

    @Override
    public ValueWrapper visit(AppendNode.Context ctx) {
        ValueWrapper sliceObj = Visit(ctx.slice);
        if (!(sliceObj instanceof SliceValue sv)) {
            this.errors.add(new GoliteError("Semantico", "append solo se puede usar con slices, obtenido: " + sliceObj.getTypeName(), sliceObj.line(), sliceObj.column()));
            return defaultVoid;
        }

        ValueWrapper newValue = Visit(ctx.element);
        
        // Verificación de tipo para el nuevo valor
        if (!sv.elementType().equals(newValue.getTypeName())) {
            this.errors.add(new GoliteError("Semantico", "Tipo incompatible en append. Se esperaba: " + sv.elementType() + ", obtenido: " + newValue.getTypeName(), newValue.line(), newValue.column()));
            return sv;
        }
        
        sv.value().add(newValue);
        return sv;
    }

    @Override
    public ValueWrapper visit(LenNode.Context ctx) {
        ValueWrapper val = Visit(ctx.expression);
        if (val instanceof SliceValue sv) {
            return new IntValue(sv.value().size(), val.line(), val.column());
        } else {
            this.errors.add(new GoliteError("Semantico", "len() solo se puede aplicar a slices, obtenido: " + val.getTypeName(), val.line(), val.column()));
            return new IntValue(0, val.line(), val.column());
        }
    }

    @Override
    public ValueWrapper visit(SliceLiteralNode.Context ctx) {
        List<ValueWrapper> list = new ArrayList<>();
        int line = -1;
        int col = -1;
        for (ASTNode node : ctx.elements) {
            ValueWrapper val = Visit(node);
            if (line == -1) {
                line = val.line();
                col = val.column();
            }
            list.add(val);
        }
        return new SliceValue(list, ctx.elementType, line, col);
    }

    @Override
    public ValueWrapper visit(IndexAccessNode.Context ctx) {
        ValueWrapper sliceObj = Visit(ctx.slice);
        if (!(sliceObj instanceof SliceValue sv)) {
            this.errors.add(new GoliteError("Semantico", "Acceso por indice solo se puede aplicar a slices, obtenido: " + sliceObj.getTypeName(), sliceObj.line(), sliceObj.column()));
            return defaultVoid;
        }

        ValueWrapper indexObj = Visit(ctx.index);
        if (!(indexObj instanceof IntValue iv)) {
            this.errors.add(new GoliteError("Semantico", "El indice debe ser de tipo int, obtenido: " + indexObj.getTypeName(), indexObj.line(), indexObj.column()));
            return defaultVoid;
        }

        int indexVal = iv.value();
        if (indexVal < 0 || indexVal >= sv.value().size()) {
            this.errors.add(new GoliteError("Semantico", "Indice fuera de rango: " + indexVal + " para slice de longitud " + sv.value().size(), iv.line(), iv.column()));
            return defaultVoid;
        }

        return sv.value().get(indexVal);
    }

    @Override
    public ValueWrapper visit(IndexAssignNode.Context ctx) {
        ValueWrapper sliceObj = Visit(ctx.slice);
        if (!(sliceObj instanceof SliceValue sv)) {
            this.errors.add(new GoliteError("Semantico", "Solo se puede asignar en slices, obtenido: " + sliceObj.getTypeName(), sliceObj.line(), sliceObj.column()));
            return defaultVoid;
        }

        ValueWrapper indexObj = Visit(ctx.index);
        if (!(indexObj instanceof IntValue iv)) {
            this.errors.add(new GoliteError("Semantico", "El indice debe ser de tipo int, obtenido: " + indexObj.getTypeName(), indexObj.line(), indexObj.column()));
            return defaultVoid;
        }

        int indexVal = iv.value();
        if (indexVal < 0 || indexVal >= sv.value().size()) {
            this.errors.add(new GoliteError("Semantico", "Indice fuera de rango en asignacion: " + indexVal + " para slice de longitud " + sv.value().size(), iv.line(), iv.column()));
            return defaultVoid;
        }

        ValueWrapper valueObj = Visit(ctx.value);
        
        // Verificación de tipo
        if (!sv.elementType().equals(valueObj.getTypeName())) {
            this.errors.add(new GoliteError("Semantico", "Tipo incompatible en asignacion a indice. Se esperaba: " + sv.elementType() + ", obtenido: " + valueObj.getTypeName(), valueObj.line(), valueObj.column()));
            return defaultVoid;
        }

        sv.value().set(indexVal, valueObj);
        return valueObj;
    }

    @Override
    public ValueWrapper visit(StructLiteralNode.Context ctx) {
        System.out.println("[DEBUG] Creando struct: " + ctx.structName);
        StructSymbol structDef = this.environment.lookupStruct(ctx.structName);
        if (structDef == null) {
            this.errors.add(new GoliteError("Semantico", "Struct no declarado: " + ctx.structName, -1, -1));
            return defaultVoid;
        }

        Map<String, ValueWrapper> fieldValues = new HashMap<>();
        // Inicializar campos por defecto
        for (Map.Entry<String, String> entry : structDef.fields.entrySet()) {
            fieldValues.put(entry.getKey(), getDefaultValue(entry.getValue()));
        }

        int line = -1;
        int col = -1;
        for (int i = 0; i < ctx.fieldNames.size(); i++) {
            String name = ctx.fieldNames.get(i);
            if (!structDef.fields.containsKey(name)) {
                this.errors.add(new GoliteError("Semantico", "Campo '" + name + "' no pertenece al struct " + ctx.structName, -1, -1));
                continue;
            }
            ValueWrapper val = Visit(ctx.values.get(i));
            if (line == -1) {
                line = val.line();
                col = val.column();
            }
            // Validar compatibilidad de tipo
            String expectedType = structDef.fields.get(name);
            if (!expectedType.equals(val.getTypeName())) {
                this.errors.add(new GoliteError("Semantico", "Tipo incompatible para el campo '" + name + "'. Se esperaba " + expectedType + ", obtenido: " + val.getTypeName(), val.line(), val.column()));
            }
            fieldValues.put(name, val);
        }
        
        return new StructInstanceValue(ctx.structName, fieldValues, line, col);
    }

    @Override
    public ValueWrapper visit(StructDeclarationNode.Context ctx) {
        this.environment.insertStruct(ctx.name, ctx.fields);
        // Registrar el struct en la tabla de simbolos para el reporte
        String scopeName = environment.getScopeName();
        this.symbols.add(new Symbol(ctx.name, "Struct", "struct", scopeName, -1, -1));
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(FieldAccessNode.Context ctx) {
        ValueWrapper structObj = Visit(ctx.structExpr);
        if (!(structObj instanceof StructInstanceValue siv)) {
            this.errors.add(new GoliteError("Semantico", "No se puede acceder a campo en tipo no-struct, obtenido: " + structObj.getTypeName(), structObj.line(), structObj.column()));
            return defaultVoid;
        }

        if (!siv.fields().containsKey(ctx.fieldName)) {
            this.errors.add(new GoliteError("Semantico", "Campo '" + ctx.fieldName + "' no existe en el struct " + siv.structName(), siv.line(), siv.column()));
            return defaultVoid;
        }

        return siv.fields().get(ctx.fieldName);
    }

    @Override
    public ValueWrapper visit(FieldAssignNode.Context ctx) {
        ValueWrapper structObj = Visit(ctx.structExpr);
        if (!(structObj instanceof StructInstanceValue siv)) {
            this.errors.add(new GoliteError("Semantico", "Solo se puede asignar campos en structs, obtenido: " + structObj.getTypeName(), structObj.line(), structObj.column()));
            return defaultVoid;
        }

        if (!siv.fields().containsKey(ctx.fieldName)) {
            this.errors.add(new GoliteError("Semantico", "Campo '" + ctx.fieldName + "' no existe en el struct " + siv.structName(), siv.line(), siv.column()));
            return defaultVoid;
        }

        ValueWrapper valueObj = Visit(ctx.value);
        
        // Verificación de tipo
        StructSymbol structDef = this.environment.lookupStruct(siv.structName());
        if (structDef != null) {
            String expectedType = structDef.fields.get(ctx.fieldName);
            if (expectedType != null && !expectedType.equals(valueObj.getTypeName())) {
                this.errors.add(new GoliteError("Semantico", "Tipo incompatible en asignacion de campo. Se esperaba: " + expectedType + ", obtenido: " + valueObj.getTypeName(), valueObj.line(), valueObj.column()));
            }
        }

        siv.fields().put(ctx.fieldName, valueObj);
        return valueObj;
    }

    @Override
    public ValueWrapper visit(MethodDeclarationNode.Context ctx) {
        StructSymbol structDef = this.environment.lookupStruct(ctx.receiverType);
        if (structDef == null) {
            this.errors.add(new GoliteError("Semantico", "Struct no declarado: " + ctx.receiverType + " para el metodo " + ctx.name, 1, 1));
        } else {
            if (structDef.methods.containsKey(ctx.name)) {
                this.errors.add(new GoliteError("Semantico", "Metodo '" + ctx.name + "' ya declarado para el struct " + ctx.receiverType, 1, 1));
            } else {
                structDef.methods.put(ctx.name, new MethodSymbol(ctx.receiverName, ctx.receiverType, ctx.name, ctx.returnType, ctx.parameters, ctx.body));
                // Registrar el metodo en la tabla de simbolos para el reporte
                String returnType = (ctx.returnType == null || ctx.returnType.isEmpty()) ? "void" : ctx.returnType;
                String scopeName = ctx.receiverType;
                this.symbols.add(new Symbol(ctx.receiverType + "." + ctx.name, "Metodo", returnType, scopeName, -1, -1));
            }
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(MethodCallNode.Context ctx) {
        // 1. Evaluar el receptor
        ValueWrapper targetVal = Visit(ctx.target);
        if (!(targetVal instanceof StructInstanceValue siv)) {
            this.errors.add(new GoliteError("Semantico", "No se puede llamar a metodo en un tipo no-struct, obtenido: " + targetVal.getTypeName(), targetVal.line(), targetVal.column()));
            return defaultVoid;
        }

        // 2. Buscar definicion del struct
        StructSymbol structDef = this.environment.lookupStruct(siv.structName());
        if (structDef == null) {
            this.errors.add(new GoliteError("Semantico", "Struct no declarado: " + siv.structName(), ctx.line, ctx.column));
            return defaultVoid;
        }

        // 3. Buscar el metodo en el struct
        MethodSymbol method = structDef.methods.get(ctx.methodName);
        if (method == null) {
            this.errors.add(new GoliteError("Semantico", "Metodo '" + ctx.methodName + "' no definido en struct " + siv.structName(), ctx.line, ctx.column));
            return defaultVoid;
        }

        // 4. Verificar cantidad de argumentos
        if (ctx.arguments.size() != method.parameters.size()) {
            this.errors.add(new GoliteError("Semantico", "Cantidad incorrecta de argumentos en llamada a metodo " + ctx.methodName + ". Se esperaban: " + method.parameters.size() + ", obtenido: " + ctx.arguments.size(), ctx.line, ctx.column));
            return defaultVoid;
        }

        // 5. Evaluar los argumentos en el ambito actual
        List<ValueWrapper> evaluatedArgs = new ArrayList<>();
        for (ASTNode arg : ctx.arguments) {
            evaluatedArgs.add(Visit(arg));
        }

        // 6. Buscar el ambito global (lexical scoping)
        Enviroment globalEnv = this.environment;
        while (globalEnv.getParent() != null) {
            globalEnv = globalEnv.getParent();
        }

        // 7. Crear el entorno local para la ejecucion del metodo
        Enviroment methodEnv = new Enviroment(globalEnv, "Metodo " + siv.structName() + "." + ctx.methodName);

        // 8. Declarar el receptor como variable local
        methodEnv.declare(method.receiverName, siv);
        this.symbols.add(new Symbol(method.receiverName, "Variable", siv.getTypeName(), methodEnv.getScopeName(), ctx.line, ctx.column));

        // 9. Registrar parametros como variables locales
        for (int i = 0; i < method.parameters.size(); i++) {
            ParameterNode param = method.parameters.get(i);
            ValueWrapper val = evaluatedArgs.get(i);
            methodEnv.declare(param.name, val);
            
            String typeName = val.getTypeName();
            if ("decimal".equals(typeName)) {
                typeName = "float64";
            }
            this.symbols.add(new Symbol(param.name, "Variable", typeName, methodEnv.getScopeName(), ctx.line, ctx.column));
        }

        // 10. Guardar estado de retorno previo
        ValueWrapper oldReturnValue = this.returnValue;
        boolean oldReturning = this.returning;

        // Reiniciar variables de retorno
        this.returnValue = defaultVoid;
        this.returning = false;

        // 11. Guardar entorno del invocador y ejecutar cuerpo
        Enviroment callerEnv = this.environment;
        this.environment = methodEnv;

        // Protegemos la ejecucion del cuerpo del metodo con try-catch para capturar
        // errores semanticos no recuperables que puedan surgir dentro del cuerpo.
        try {
            if (method.body != null) {
                Visit(method.body);
            }
        } catch (SemanticException e) {
            this.errors.add(new GoliteError(
                "Semantico",
                "Error en metodo '" + siv.structName() + "." + ctx.methodName + "': " + e.getMessage(),
                e.getLine(),
                e.getColumn()));
        } finally {
            this.environment = callerEnv;
        }

        // Capturar resultado
        ValueWrapper resultValue = this.returnValue;

        // Restaurar estado de retorno previo
        this.returnValue = oldReturnValue;
        this.returning = oldReturning;

        return resultValue;
    }

    private ValueWrapper getDefaultValue(String type) {
        return switch (type) {
            case "int" -> new IntValue(0, -1, -1);
            case "float64" -> new DecimalValue(0.0, -1, -1);
            case "string" -> new StringValue("\"\"", -1, -1);
            case "bool" -> new BoolValue(false, -1, -1);
            case "rune" -> new RuneValue('\0', -1, -1);
            default -> new NilValue(-1, -1);
        };
    }

    private boolean areEqual(ValueWrapper left, ValueWrapper right) {
        if (left instanceof IntValue li && right instanceof IntValue ri) {
            return li.value() == ri.value();
        }
        if (left instanceof DecimalValue ld && right instanceof DecimalValue rd) {
            return ld.value() == rd.value();
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return l.value().equals(r.value());
        }
        if (left instanceof BoolValue l && right instanceof BoolValue r) {
            return l.value() == r.value();
        }
        if (left instanceof RuneValue l && right instanceof RuneValue r) {
            return l.value() == r.value();
        }
        if (left instanceof NilValue && right instanceof NilValue) {
            return true;
        }
        return false;
    }
}
