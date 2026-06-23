package olc1.golite;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.reports.GoliteError;
import olc1.golite.reports.HtmlReportGenerator;
import olc1.golite.visitor.interpreter.InterpreterVisitor;

// Clase de pruebas automatizadas.
// La escribí para validar la correcta generación de reportes HTML y evaluar flujos complejos de mi intérprete.
public class TestReports {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DE REPORTES GOLITE ===");

        // --- PRUEBA 1: PROGRAMA VALIDO SINTACTICAMENTE CON ANALISIS SEMANTICO Y SIMBOLOS ---
        System.out.println("\n--- PRUEBA 1: Analisis Semantico y Simbolos ---");
        String code1 = "func main() {\n" +
                       "    // 1. Concatenacion de strings\n" +
                       "    s1 := \"Hola \";\n" +
                       "    s2 := \"Mundo!\";\n" +
                       "    s3 := s1 + s2;\n" +
                       "    fmt.Println(s3);\n" +
                       "    \n" +
                       "    // 2. Funciones embebidas\n" +
                       "    valInt := strconv.Atoi(\"123\");\n" +
                       "    valFloat := strconv.ParseFloat(\"45.67\");\n" +
                       "    fmt.Println(valInt);\n" +
                       "    fmt.Println(valFloat);\n" +
                       "    fmt.Println(reflect.TypeOf(valInt));\n" +
                       "    fmt.Println(reflect.TypeOf(valFloat));\n" +
                       "    fmt.Println(reflect.TypeOf(s3));\n" +
                       "    \n" +
                       "    // 3. Division por cero (provoca error semantico)\n" +
                       "    divZero := 10 / 0;\n" +
                       "    modZero := 10 % 0;\n" +
                       "    \n" +
                       "    // 4. Errores de validacion de tipos estáticos (provoca errores semanticos)\n" +
                       "    var x int = 10;\n" +
                       "    x = 20.5;\n" + // Error: int = decimal
                       "    invalidAdd := 10 + 20.5;\n" + // Error: int + decimal
                       "}\n";













        Lexer lexer1 = null;
        parser parser1 = null;
        InterpreterVisitor interpreter1 = null;

        try {
            lexer1 = new Lexer(new BufferedReader(new StringReader(code1)));
            parser1 = new parser(lexer1);

            ASTNode ast1 = (ASTNode) parser1.parse().value;
            interpreter1 = new InterpreterVisitor();
            
            if (ast1 != null) {
                interpreter1.Visit(ast1);
            }

            System.out.println("Prueba 1 completada.");
            System.out.println("- Tokens escaneados: " + lexer1.tokens.size());
            System.out.println("- Simbolos detectados (unicos): " + interpreter1.symbols.size());
            System.out.println("- Errores semanticos: " + interpreter1.errors.size());
            System.out.println("- Salida consola:");
            System.out.print(interpreter1.output);
            for (var sym : interpreter1.symbols) {
                System.out.printf("  [Simbolo] Nombre: %s, Tipo: %s, Ambito: %s, Pos: %d:%d\n",
                    sym.getName(), sym.getDataType(), sym.getScope(), sym.getLine(), sym.getColumn());
            }
            for (var err : interpreter1.errors) {
                System.out.printf("  [Error Semantico] %s, Pos: %d:%d\n",
                    err.getDescription(), err.getLine(), err.getColumn());
            }
        } catch (Exception ex) {
            System.err.println("Error en Prueba 1: " + ex.getMessage());
        }

        // --- PRUEBA 2: PROGRAMA CON ERRORES LEXICOS Y SINTACTICOS ---
        System.out.println("\n--- PRUEBA 2: Errores Lexicos y Sintacticos ---");
        // No usar comentarios, solo sentencias erroneas
        String code2 = "imprimir(5 + );\n" + // Error Sintactico
                       "$\n" +              // Error Lexico
                       "imprimir(10);\n";   // Sentencia correcta

        Lexer lexer2 = null;
        parser parser2 = null;

        try {
            lexer2 = new Lexer(new BufferedReader(new StringReader(code2)));
            parser2 = new parser(lexer2);
            parser2.parse();
            System.out.println("Prueba 2 completada.");
            System.out.println("- Errores lexicos: " + lexer2.errors.size());
            System.out.println("- Errores sintacticos: " + parser2.errors.size());
            for (var err : lexer2.errors) {
                System.out.printf("  [Error Lexico] %s, Pos: %d:%d\n", err.getDescription(), err.getLine(), err.getColumn());
            }
            for (var err : parser2.errors) {
                System.out.printf("  [Error Sintactico] %s, Pos: %d:%d\n", err.getDescription(), err.getLine(), err.getColumn());
            }
        } catch (Exception ex) {
            System.out.println("Prueba 2 finalizo (con excepciones esperadas): " + ex.getMessage());
        }

        // --- PRUEBA 3: SLICES ---
        System.out.println("\n--- PRUEBA 3: Slices ---");
        String code3 = "func main() {\n" +
                       "    numeros := []int{10, 20, 30, 40};\n" +
                       "    fmt.Println(\"Original:\", numeros[1]);\n" +
                       "    \n" +
                       "    numeros[2] = 99;\n" +
                       "    fmt.Println(\"Después de asignación:\", numeros[2]);\n" +
                       "    \n" +
                       "    numeros = append(numeros, 50);\n" +
                       "    fmt.Println(\"Después de append:\", len(numeros));\n" +
                       "}\n";

        Lexer lexer3 = null;
        parser parser3 = null;
        InterpreterVisitor interpreter3 = null;

        try {
            lexer3 = new Lexer(new BufferedReader(new StringReader(code3)));
            parser3 = new parser(lexer3);

            ASTNode ast3 = (ASTNode) parser3.parse().value;
            interpreter3 = new InterpreterVisitor();
            
            if (ast3 != null) {
                interpreter3.Visit(ast3);
            }

            System.out.println("Prueba 3 completada.");
            System.out.println("- Salida consola:");
            System.out.print(interpreter3.output);
        } catch (Exception ex) {
            System.err.println("Error en Prueba 3: " + ex.getMessage());
            ex.printStackTrace();
        }

        // --- CONSOLIDACION Y GENERACION DE REPORTES ---
        System.out.println("\n--- Generando Reportes Consolidados ---");
        try {
            List<GoliteError> consolidatedErrors = new ArrayList<>();
            if (lexer1 != null) consolidatedErrors.addAll(lexer1.errors);
            if (parser1 != null) consolidatedErrors.addAll(parser1.errors);
            if (interpreter1 != null) consolidatedErrors.addAll(interpreter1.errors);
            if (lexer2 != null) consolidatedErrors.addAll(lexer2.errors);
            if (parser2 != null) consolidatedErrors.addAll(parser2.errors);

            File errReport = new File("reports/test_reporte_errores.html");
            File tokReport = new File("reports/test_reporte_tokens.html");
            File symReport = new File("reports/test_reporte_tabla_simbolos.html");

            HtmlReportGenerator.generateErrorsReport(errReport, consolidatedErrors);
            HtmlReportGenerator.generateTokensReport(tokReport, lexer1.tokens);
            HtmlReportGenerator.generateSymbolsReport(symReport, interpreter1.symbols);

            System.out.println("\n=== REPORTES HTML GENERADOS EXITOSAMENTE ===");
            System.out.println("- Errores: " + errReport.getAbsolutePath());
            System.out.println("- Tokens: " + tokReport.getAbsolutePath());
            System.out.println("- Simbolos: " + symReport.getAbsolutePath());

        } catch (Exception ex) {
            System.err.println("Error al generar reportes: " + ex.getMessage());
        }
    }
}
