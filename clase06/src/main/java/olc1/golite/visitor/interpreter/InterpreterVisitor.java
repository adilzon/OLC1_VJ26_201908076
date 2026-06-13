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

        return switch (left) {
            case IntValue l when right instanceof IntValue     r -> 
                new IntValue(l.value() + r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> 
                new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() + r.value(), l.line(), l.column());
            default -> 
                throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " + " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Sub.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue     r -> 
                new IntValue((int)(l.value() - r.value()), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() - r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> 
                new DecimalValue(l.value() - r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() - r.value(), l.line(), l.column());
            default -> 
                throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " - " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Mul.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue     r -> 
                new IntValue(l.value() * r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() * r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> 
                new DecimalValue(l.value() * r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() * r.value(), l.line(), l.column());
            default -> 
                throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " * " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Div.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue     r -> 
                new IntValue(l.value() / r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> 
                new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> 
                new DecimalValue(l.value() / r.value(), l.line(), l.column());
            default -> 
                throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " / " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Lt.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue     r -> 
                new BoolValue(l.value() < r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r -> 
                new BoolValue(l.value() < r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> 
                new BoolValue(l.value() < r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> 
                new BoolValue(l.value() < r.value(), l.line(), l.column());
            default -> 
                throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " < " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Negate.Context ctx) {
        ValueWrapper operand = Visit(ctx.expression);
        return switch (operand) {
            case IntValue     v -> new IntValue(-v.value(), v.line(), v.column());
            case DecimalValue v -> new DecimalValue(-v.value(), v.line(), v.column());
            default -> throw new RuntimeException("Operacion invalida: -" + operand.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Imprimir.Context ctx) {
        ValueWrapper value = Visit(ctx.expression);
        output += value.toString() + "\n";
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
            environment.declare(ctx.name, val);
            boolean exists = false;
            String scopeName = environment.getScopeName();
            for (Symbol s : this.symbols) {
                if (s.getName().equals(ctx.name) && s.getScope().equals(scopeName) && s.getLine() == ctx.line && s.getColumn() == ctx.column) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                this.symbols.add(new Symbol(ctx.name, "Variable", val.getTypeName(), scopeName, ctx.line, ctx.column));
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
        
        ValueWrapper cond = Visit(ctx.condition);
        if (cond instanceof BoolValue b && b.value()) {
            this.environment = new Enviroment(parentEnv, "If");
            Visit(ctx.body);
            this.environment = parentEnv;
            return defaultVoid;
        } 

        ElifNodes elifs = ctx.elifList;

        if (elifs != null) {
            Visit(elifs);

            for (ElifNode elif : elifs.ctx.elifNodesList) {
                Visit(elif);
                ValueWrapper elifCond = Visit(elif.ctx.condition);

                if (elifCond instanceof BoolValue elifB && elifB.value()) {
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
        // Logica implemantada en otro nodo
        return defaultVoid;
    }
    
    @Override
    public ValueWrapper visit(ElifNode.Context ctx) {
        // Logica implemantada en otro nodo
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(WhileFor.Context ctx) {
        Enviroment parentEnv = this.environment;
        ValueWrapper condition = Visit(ctx.condition);

        while (condition instanceof BoolValue b && b.value()) {
            this.environment = new Enviroment(parentEnv, "For");

            try {
                Visit(ctx.body);
            } catch (BreakException e) {
                break;
            }
           
            condition = Visit(ctx.condition);
        }

        this.environment = parentEnv;
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Breakstm.Context ctx) {
        throw new BreakException();
    }
}
