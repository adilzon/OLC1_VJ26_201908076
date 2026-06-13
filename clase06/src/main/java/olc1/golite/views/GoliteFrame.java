package olc1.golite.views;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import olc1.golite.Lexer;
import olc1.golite.parser;
import olc1.golite.ast.ASTNode;
import olc1.golite.reports.GoliteError;
import olc1.golite.reports.HtmlReportGenerator;
import olc1.golite.reports.Symbol;
import olc1.golite.reports.Token;
import olc1.golite.visitor.interpreter.InterpreterVisitor;

public class GoliteFrame extends JFrame {
    private final EditorPanel editorPanel;
    private final JTextArea consoleTextArea;
    private Lexer lexer;
    private parser parser;
    InterpreterVisitor interpreter;

    public GoliteFrame() {
        setTitle("Golite");
        setMinimumSize(new Dimension(600, 400));
        setSize(new Dimension(1200, 675));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        editorPanel = new EditorPanel();
        consoleTextArea = new JTextArea();
        cleanConsole();

        GoliteMenuBar menuBar = new GoliteMenuBar();
        setJMenuBar(menuBar);
        add(new MainPanel(editorPanel, consoleTextArea));

        wireActions(menuBar);

        setVisible(true);
        editorPanel.getTextArea().requestFocus();
    }

    private void wireActions(GoliteMenuBar menuBar) {
        menuBar.onRun(e -> run());
        menuBar.onClean(e -> cleanConsole());
        menuBar.onNew(e -> editorPanel.setText("imprimir(5+5);\n"));
        menuBar.onExit(e -> System.exit(0));
        menuBar.onTokens(e -> showTokensReport());
        menuBar.onSymbols(e -> showSymbolsReport());
        menuBar.onErrors(e -> showErrorsReport());
        menuBar.onAbout(e -> JOptionPane.showMessageDialog(
                this,
                "GolLite\nVersión 1.0.0\nLaboratorio OLC1",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void run() {
        try {
            lexer = new Lexer(new BufferedReader(new StringReader(editorPanel.getText())));
            parser = new parser(lexer);

            ASTNode ast = (ASTNode) parser.parse().value;
            interpreter = new InterpreterVisitor();
            interpreter.Visit(ast);

            cleanConsole();
            consoleTextArea.append(interpreter.output);
        } catch (Exception e) {
            consoleTextArea.append("Error: " + e.getMessage() + "\n");
        }
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        editorPanel.getTextArea().requestFocus();
    }

    private void errors() {
        cleanConsole();

        if (lexer == null || parser == null) {
            consoleTextArea.append("Aún no se han ejecutado nada.\n");
            return;
        }

        consoleTextArea.append("Errores léxicos:\n");

        for (GoliteError error : lexer.errors) {
            consoleTextArea.append(error.toString() + "\n");
        }

        consoleTextArea.append("\nErrores sintácticos:\n");

        for (GoliteError error : parser.errors) {
            consoleTextArea.append(error.toString() + "\n");
        }

        consoleTextArea.append("\nErrores semánticos:\n");
        if (interpreter != null && interpreter.errors != null) {
            for (GoliteError error : interpreter.errors) {
                consoleTextArea.append(error.toString() + "\n");
            }
        }
    }

    private void cleanConsole() {
        consoleTextArea.setText("CONSOLA  -  LABORATORIO DE ORGANIZACION DE LENGUAJES Y COMPILADORES 1\n\n");
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    public JTextArea getConsoleTextArea() {
        return consoleTextArea;
    }

    private void showErrorsReport() {
        if (lexer == null || parser == null) {
            JOptionPane.showMessageDialog(this, "Aún no se ha realizado ningún análisis.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<GoliteError> allErrors = new ArrayList<>();
        if (lexer.errors != null) {
            allErrors.addAll(lexer.errors);
        }
        if (parser.errors != null) {
            allErrors.addAll(parser.errors);
        }
        if (interpreter != null && interpreter.errors != null) {
            allErrors.addAll(interpreter.errors);
        }

        try {
            File reportFile = new File("reports/reporte_errores.html");
            HtmlReportGenerator.generateErrorsReport(reportFile, allErrors);
            openInBrowser(reportFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de errores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTokensReport() {
        if (lexer == null) {
            JOptionPane.showMessageDialog(this, "Aún no se ha realizado ningún análisis.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            File reportFile = new File("reports/reporte_tokens.html");
            HtmlReportGenerator.generateTokensReport(reportFile, lexer.tokens);
            openInBrowser(reportFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de tokens: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSymbolsReport() {
        if (interpreter == null) {
            JOptionPane.showMessageDialog(this, "Aún no se ha realizado ningún análisis.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            File reportFile = new File("reports/reporte_tabla_simbolos.html");
            HtmlReportGenerator.generateSymbolsReport(reportFile, interpreter.symbols);
            openInBrowser(reportFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar tabla de símbolos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openInBrowser(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(file.toURI());
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo abrir el navegador automáticamente.\nEl reporte se guardó en:\n" + file.getAbsolutePath(), 
                    "Reporte Generado", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al abrir el reporte: " + ex.getMessage() + "\nArchivo: " + file.getAbsolutePath(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
