package olc1.golite.views;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import olc1.golite.Lexer;
import olc1.golite.parser;
import olc1.golite.ast.ASTNode;
import olc1.golite.reports.GoliteError;
import olc1.golite.reports.HtmlReportGenerator;
import olc1.golite.reports.Symbol;
import olc1.golite.reports.Token;
import olc1.golite.visitor.AstJsonVisitor;
import olc1.golite.visitor.interpreter.InterpreterVisitor;

public class GoliteFrame extends JFrame {
    private final EditorPanel editorPanel;
    private final JTextArea consoleTextArea;
    private Lexer lexer;
    private parser parser;
    InterpreterVisitor interpreter;
    private ASTNode lastAst = null;

    private File currentFile = null;
    private boolean isModified = false;
    private boolean isProgrammaticChange = false;

    public GoliteFrame() {
        setTitle("Golite");
        setMinimumSize(new Dimension(600, 400));
        setSize(new Dimension(1200, 675));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });
        setLocationRelativeTo(null);

        editorPanel = new EditorPanel();
        consoleTextArea = new JTextArea();
        cleanConsole();

        GoliteMenuBar menuBar = new GoliteMenuBar();
        setJMenuBar(menuBar);
        add(new MainPanel(editorPanel, consoleTextArea));

        wireActions(menuBar);

        editorPanel.getTextArea().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isProgrammaticChange) {
                    isModified = true;
                    updateTitle();
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isProgrammaticChange) {
                    isModified = true;
                    updateTitle();
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isProgrammaticChange) {
                    isModified = true;
                    updateTitle();
                }
            }
        });

        updateTitle();
        setVisible(true);
        editorPanel.getTextArea().requestFocus();
    }

    private void updateTitle() {
        String filename = (currentFile == null) ? "Sin Título" : currentFile.getName();
        String modifiedIndicator = isModified ? " *" : "";
        setTitle("Golite - " + filename + modifiedIndicator);
    }

    private void setEditorText(String text) {
        isProgrammaticChange = true;
        editorPanel.setText(text);
        isProgrammaticChange = false;
    }

    private void newFile() {
        if (confirmDiscardChanges()) {
            setEditorText("");
            currentFile = null;
            isModified = false;
            updateTitle();
        }
    }

    private void openFile() {
        if (!confirmDiscardChanges()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] encoded = Files.readAllBytes(selectedFile.toPath());
                String content = new String(encoded, StandardCharsets.UTF_8);
                setEditorText(content);
                currentFile = selectedFile;
                isModified = false;
                updateTitle();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al leer el archivo: " + e.getMessage(), 
                    "Error de lectura", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean saveFile() {
        if (currentFile != null) {
            try {
                Files.write(currentFile.toPath(), editorPanel.getText().getBytes(StandardCharsets.UTF_8));
                isModified = false;
                updateTitle();
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al guardar el archivo: " + e.getMessage(), 
                    "Error al guardar", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            return saveAsFile();
        }
    }

    private boolean saveAsFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".glt")) {
                selectedFile = new File(path + ".glt");
            }

            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "El archivo ya existe. ¿Desea sobrescribirlo?", 
                    "Confirmar sobrescritura", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            try {
                Files.write(selectedFile.toPath(), editorPanel.getText().getBytes(StandardCharsets.UTF_8));
                currentFile = selectedFile;
                isModified = false;
                updateTitle();
                JOptionPane.showMessageDialog(this, 
                    "Archivo guardado exitosamente.", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al guardar el archivo: " + e.getMessage(), 
                    "Error al guardar", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private boolean confirmDiscardChanges() {
        if (!isModified) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "El archivo actual tiene cambios no guardados. ¿Desea guardarlos?",
            "Cambios no guardados",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            return saveFile();
        } else if (choice == JOptionPane.NO_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    private void exitApp() {
        if (confirmDiscardChanges()) {
            System.exit(0);
        }
    }

    private void wireActions(GoliteMenuBar menuBar) {
        menuBar.onRun(e -> run());
        menuBar.onClean(e -> cleanConsole());
        menuBar.onNew(e -> newFile());
        menuBar.onOpen(e -> openFile());
        menuBar.onSave(e -> saveFile());
        menuBar.onSaveAs(e -> saveAsFile());
        menuBar.onExit(e -> exitApp());
        menuBar.onTokens(e -> showTokensReport());
        menuBar.onSymbols(e -> showSymbolsReport());
        menuBar.onErrors(e -> showErrorsReport());
        menuBar.onAst(e -> showAstReport());
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
            lastAst = ast;  // Guardar el AST para el reporte
            interpreter = new InterpreterVisitor();
            
            if (ast != null && lexer.errors.isEmpty() && parser.errors.isEmpty()) {
                interpreter.Visit(ast);
            }

            cleanConsole();
            if (!lexer.errors.isEmpty() || !parser.errors.isEmpty() || (interpreter != null && !interpreter.errors.isEmpty())) {
                errors();
            } else {
                consoleTextArea.append(interpreter.output);
            }
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

    private void showAstReport() {
        if (lastAst == null) {
            JOptionPane.showMessageDialog(this, "Aún no se ha realizado ningún análisis.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            AstJsonVisitor astVisitor = new AstJsonVisitor();
            String json = lastAst.accept(astVisitor);
            File reportFile = new File("reports/reporte_ast.html");
            HtmlReportGenerator.generateAstReport(reportFile, json);
            openInBrowser(reportFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de AST: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
