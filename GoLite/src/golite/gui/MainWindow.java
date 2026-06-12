package golite.gui;

import golite.errors.ErrorHandler;
import golite.errors.ErrorHandler.TokenEntry;
import golite.interpreter.Interprete;
import golite.lexer.Lexer;
import golite.parser.Parser;
import golite.ast.Nodo;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;

public class MainWindow extends JFrame {

    // ===== COMPONENTES =====
    private JTabbedPane tabbedPane;        // pestañas de archivos
    private JTextArea consola;             // área de salida
    private JLabel labelLinea;             // muestra línea actual
    private ArrayList<TokenEntry> tokens = new ArrayList<>();

    public MainWindow() {
        initComponents();
    }

    private void initComponents() {
        setTitle("GoLite IDE");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== MENÚ =====
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemNuevo = new JMenuItem("Nuevo");
        JMenuItem itemAbrir = new JMenuItem("Abrir");
        JMenuItem itemGuardar = new JMenuItem("Guardar");
        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemGuardar);

        JMenu menuReportes = new JMenu("Reportes");
        JMenuItem itemErrores = new JMenuItem("Reporte de Errores");
        JMenuItem itemTokens = new JMenuItem("Tabla de Tokens");
        menuReportes.add(itemErrores);
        menuReportes.add(itemTokens);

        menuBar.add(menuArchivo);
        menuBar.add(menuReportes);

        // ===== BOTÓN EJECUTAR =====
        JButton btnEjecutar = new JButton("▶ Ejecutar");
        btnEjecutar.setBackground(new Color(0, 120, 0));
        btnEjecutar.setForeground(Color.WHITE);
        btnEjecutar.setFont(new Font("Arial", Font.BOLD, 13));
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(btnEjecutar);

        setJMenuBar(menuBar);

        // ===== PANEL PRINCIPAL =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.7);

        // ===== EDITOR CON PESTAÑAS =====
        tabbedPane = new JTabbedPane();
        agregarNuevaPestana("nuevo.glt", "");
        splitPane.setTopComponent(tabbedPane);

        // ===== CONSOLA =====
        JPanel panelConsola = new JPanel(new BorderLayout());
        JLabel labelConsola = new JLabel("  Consola");
        labelConsola.setFont(new Font("Arial", Font.BOLD, 12));
        labelConsola.setOpaque(true);
        labelConsola.setBackground(new Color(50, 50, 50));
        labelConsola.setForeground(Color.WHITE);
        labelConsola.setPreferredSize(new Dimension(0, 25));

        consola = new JTextArea();
        consola.setEditable(false);
        consola.setBackground(new Color(30, 30, 30));
        consola.setForeground(new Color(0, 255, 0));
        consola.setFont(new Font("Consolas", Font.PLAIN, 13));
        consola.setCaretColor(Color.WHITE);

        panelConsola.add(labelConsola, BorderLayout.NORTH);
        panelConsola.add(new JScrollPane(consola), BorderLayout.CENTER);
        splitPane.setBottomComponent(panelConsola);

        // ===== BARRA DE ESTADO =====
        JPanel barraEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelLinea = new JLabel("Línea: 1 | Columna: 1");
        barraEstado.add(labelLinea);

        add(splitPane, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        // ===== ACCIONES =====
        itemNuevo.addActionListener(e -> crearNuevoArchivo());
        itemAbrir.addActionListener(e -> abrirArchivo());
        itemGuardar.addActionListener(e -> guardarArchivo());
        btnEjecutar.addActionListener(e -> ejecutar());
        itemErrores.addActionListener(e -> mostrarReporteErrores());
        itemTokens.addActionListener(e -> mostrarTablaTokens());
    }

    // ===== CREAR NUEVA PESTAÑA =====
    private void agregarNuevaPestana(String titulo, String contenido) {
        JPanel panel = new JPanel(new BorderLayout());

        // Números de línea + editor
        JTextArea editor = new JTextArea(contenido);
        editor.setFont(new Font("Consolas", Font.PLAIN, 14));
        editor.setTabSize(4);

        JTextArea numerosLinea = new JTextArea("1");
        numerosLinea.setFont(new Font("Consolas", Font.PLAIN, 14));
        numerosLinea.setBackground(new Color(230, 230, 230));
        numerosLinea.setForeground(Color.GRAY);
        numerosLinea.setEditable(false);
        numerosLinea.setMargin(new Insets(0, 5, 0, 5));

        // Actualizar números de línea
        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeros(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeros(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeros(); }

            private void actualizarNumeros() {
                int lineas = editor.getLineCount();
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= lineas; i++) {
                    sb.append(i).append("\n");
                }
                numerosLinea.setText(sb.toString());
            }
        });

        // Actualizar posición del cursor
        editor.addCaretListener(e -> {
            try {
                int pos = editor.getCaretPosition();
                int linea = editor.getLineOfOffset(pos) + 1;
                int col = pos - editor.getLineStartOffset(linea - 1) + 1;
                labelLinea.setText("Línea: " + linea + " | Columna: " + col);
            } catch (Exception ex) {}
        });

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setRowHeaderView(numerosLinea);
        panel.add(scroll, BorderLayout.CENTER);

        // Botón cerrar pestaña
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);
        JLabel tabTitulo = new JLabel(titulo + "  ");
        JButton btnCerrar = new JButton("✕");
        btnCerrar.setPreferredSize(new Dimension(16, 16));
        btnCerrar.setMargin(new Insets(0, 0, 0, 0));
        btnCerrar.setFont(new Font("Arial", Font.PLAIN, 10));
        tabHeader.add(tabTitulo);
        tabHeader.add(btnCerrar);

        tabbedPane.addTab(titulo, panel);
        int index = tabbedPane.indexOfComponent(panel);
        tabbedPane.setTabComponentAt(index, tabHeader);
        tabbedPane.setSelectedIndex(index);

        btnCerrar.addActionListener(e -> {
            int i = tabbedPane.indexOfComponent(panel);
            if (i >= 0) tabbedPane.removeTabAt(i);
        });
    }

    // ===== OBTENER EDITOR ACTIVO =====
    private JTextArea getEditorActivo() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx < 0) return null;
        JPanel panel = (JPanel) tabbedPane.getComponentAt(idx);
        JScrollPane scroll = (JScrollPane) panel.getComponent(0);
        return (JTextArea) scroll.getViewport().getView();
    }

    // ===== NUEVO ARCHIVO =====
    private void crearNuevoArchivo() {
        agregarNuevaPestana("nuevo.glt", "");
    }

    // ===== ABRIR ARCHIVO =====
    private void abrirArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("GoLite (*.glt)", "glt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            try {
                BufferedReader br = new BufferedReader(new FileReader(archivo));
                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    sb.append(linea).append("\n");
                }
                br.close();
                agregarNuevaPestana(archivo.getName(), sb.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir: " + ex.getMessage());
            }
        }
    }

    // ===== GUARDAR ARCHIVO =====
    private void guardarArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("GoLite (*.glt)", "glt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            if (!archivo.getName().endsWith(".glt")) {
                archivo = new File(archivo.getAbsolutePath() + ".glt");
            }
            try {
                JTextArea editor = getEditorActivo();
                if (editor == null) return;
                BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
                bw.write(editor.getText());
                bw.close();
                JOptionPane.showMessageDialog(this, "Archivo guardado correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        }
    }

    // ===== EJECUTAR =====
    private void ejecutar() {
        JTextArea editor = getEditorActivo();
        if (editor == null) {
            consola.setText("[ERROR] No hay archivo abierto.");
            return;
        }

        String codigo = editor.getText().trim();
        if (codigo.isEmpty()) {
            consola.setText("[ERROR] El editor está vacío.");
            return;
        }

        consola.setText("> Ejecutando...\n[INFO] Compilación iniciada\n");
        ErrorHandler.limpiar();
        tokens.clear();

        try {
            // Análisis léxico y sintáctico
            StringReader sr = new StringReader(codigo);
            Lexer lexer = new Lexer(sr);
            Parser parser = new Parser(lexer);
            Object resultado = parser.parse().value;

            System.out.println("RESULTADO PARSER: " + resultado);

            Nodo raiz = (Nodo) resultado;

            if (raiz == null) {
                consola.append("[ERROR] El parser devolvió NULL\n");
                return;
            }

            consola.append("[INFO] AST generado: " + raiz.tipo + "\n");

            // Recolectar tokens para el reporte
            recolectarTokens(codigo);

            if (ErrorHandler.hayErrores()) {
                consola.append("[ERROR] Se encontraron errores. Revisa el reporte.\n");
                for (ErrorHandler.ErrorEntry e : ErrorHandler.getErrores()) {
                    consola.append("[" + e.tipo + "] " + e.descripcion +
                                   " (línea " + e.linea + ")\n");
                }
                return;
            }

            // Ejecutar
            Interprete interprete = new Interprete();
            String salida = interprete.ejecutar(raiz);
            consola.append(salida);

            if (ErrorHandler.hayErrores()) {
                consola.append("\n--- Errores durante ejecución ---\n");
                for (ErrorHandler.ErrorEntry e : ErrorHandler.getErrores()) {
                    consola.append("[" + e.tipo + "] " + e.descripcion + "\n");
                }
            }

        } catch (Exception ex) {
    consola.append(
        "[ERROR SINTÁCTICO] " +
        ex.getClass().getSimpleName() +
        ": " +
        ex.getMessage() +
        "\n"
    );

    ex.printStackTrace();

    ErrorHandler.errorSintactico(
        ex.getMessage() != null
            ? ex.getMessage()
            : "Error sintáctico",
        0,
        0
    );
}
    }

    // ===== RECOLECTAR TOKENS =====
    private void recolectarTokens(String codigo) {
        try {
            StringReader sr = new StringReader(codigo);
            Lexer lexer = new Lexer(sr);
            java_cup.runtime.Symbol sym;
            while ((sym = lexer.next_token()).sym != golite.parser.sym.EOF) {
                String lexema = sym.value != null ? sym.value.toString() : "";
                String tipo = obtenerNombreToken(sym.sym);
                tokens.add(new TokenEntry(lexema, tipo, sym.left, sym.right));
            }
        } catch (Exception e) {}
    }

    private String obtenerNombreToken(int id) {
        try {
            java.lang.reflect.Field[] fields = golite.parser.sym.class.getFields();
            for (java.lang.reflect.Field f : fields) {
                if (f.getInt(null) == id) return f.getName();
            }
        } catch (Exception e) {}
        return "DESCONOCIDO";
    }

    // ===== REPORTES =====
    private void mostrarReporteErrores() {
        JFrame frame = new JFrame("Reporte de Errores");
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(this);
        JEditorPane pane = new JEditorPane("text/html", ErrorHandler.generarReporteHTML());
        pane.setEditable(false);
        frame.add(new JScrollPane(pane));
        frame.setVisible(true);
    }

    private void mostrarTablaTokens() {
        JFrame frame = new JFrame("Tabla de Tokens");
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(this);
        JEditorPane pane = new JEditorPane("text/html",
                ErrorHandler.generarTablaTokensHTML(tokens));
        pane.setEditable(false);
        frame.add(new JScrollPane(pane));
        frame.setVisible(true);
    }
}