package olc1.golite;

import java.io.BufferedReader;
import java.io.StringReader;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.interpreter.InterpreterVisitor;

public class Main {
    public static void main(String[] args) {
        try {
            String texto = "dos = 2; imprimir(5 * dos); if (true) { imprimir(20); } if (false) { imprimir(30); }";
            Lexer s = new Lexer(new BufferedReader(new StringReader(texto)));
            parser p = new parser(s);
            ASTNode ast = (ASTNode) p.parse().value;
            InterpreterVisitor interpreter = new InterpreterVisitor();
            interpreter.Visit(ast);
            System.out.print(interpreter.output);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
