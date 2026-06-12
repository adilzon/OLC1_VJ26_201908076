package golite.errors;

import java.util.ArrayList;

public class ErrorHandler {
    
    // Lista donde guardamos todos los errores encontrados
    private static ArrayList<ErrorEntry> errores = new ArrayList<>();
    
    // Clase interna que representa un error
    public static class ErrorEntry {
        public String descripcion;
        public int linea;
        public int columna;
        public String tipo; // "léxico", "sintáctico", "semántico"
        
        public ErrorEntry(String descripcion, int linea, int columna, String tipo) {
            this.descripcion = descripcion;
            this.linea = linea;
            this.columna = columna;
            this.tipo = tipo;
        }
    }
    
    // Agregar un error léxico
    public static void errorLexico(String descripcion, int linea, int columna) {
        errores.add(new ErrorEntry(descripcion, linea, columna, "léxico"));
    }
    
    // Agregar un error sintáctico
    public static void errorSintactico(String descripcion, int linea, int columna) {
        errores.add(new ErrorEntry(descripcion, linea, columna, "sintáctico"));
    }
    
    // Agregar un error semántico
    public static void errorSemantico(String descripcion, int linea, int columna) {
        errores.add(new ErrorEntry(descripcion, linea, columna, "semántico"));
    }
    
    // Obtener todos los errores
    public static ArrayList<ErrorEntry> getErrores() {
        return errores;
    }
    
    // Verificar si hay errores
    public static boolean hayErrores() {
        return !errores.isEmpty();
    }
    
    // Limpiar errores (antes de cada ejecución)
    public static void limpiar() {
        errores.clear();
    }
    
    // Generar reporte de errores en HTML
    public static String generarReporteHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>");
        sb.append("body { font-family: Arial; margin: 20px; }");
        sb.append("h2 { color: #cc0000; }");
        sb.append("table { border-collapse: collapse; width: 100%; }");
        sb.append("th { background-color: #cc0000; color: white; padding: 8px; }");
        sb.append("td { border: 1px solid #ddd; padding: 8px; }");
        sb.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        sb.append("</style></head><body>");
        sb.append("<h2>Reporte de Errores</h2>");
        
        if (errores.isEmpty()) {
            sb.append("<p style='color:green'>No se encontraron errores.</p>");
        } else {
            sb.append("<table>");
            sb.append("<tr><th>No.</th><th>Descripción</th>");
            sb.append("<th>Línea</th><th>Columna</th><th>Tipo</th></tr>");
            
            for (int i = 0; i < errores.size(); i++) {
                ErrorEntry e = errores.get(i);
                sb.append("<tr>");
                sb.append("<td>").append(i + 1).append("</td>");
                sb.append("<td>").append(e.descripcion).append("</td>");
                sb.append("<td>").append(e.linea).append("</td>");
                sb.append("<td>").append(e.columna).append("</td>");
                sb.append("<td>").append(e.tipo).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        
        sb.append("</body></html>");
        return sb.toString();
    }
    
    // Generar tabla de tokens en HTML
    public static String generarTablaTokensHTML(ArrayList<TokenEntry> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>");
        sb.append("body { font-family: Arial; margin: 20px; }");
        sb.append("h2 { color: #0066cc; }");
        sb.append("table { border-collapse: collapse; width: 100%; }");
        sb.append("th { background-color: #0066cc; color: white; padding: 8px; }");
        sb.append("td { border: 1px solid #ddd; padding: 8px; }");
        sb.append("tr:nth-child(even) { background-color: #f0f5ff; }");
        sb.append("</style></head><body>");
        sb.append("<h2>Tabla de Tokens</h2>");
        
        if (tokens.isEmpty()) {
            sb.append("<p>No se encontraron tokens.</p>");
        } else {
            sb.append("<table>");
            sb.append("<tr><th>No.</th><th>Lexema</th>");
            sb.append("<th>Tipo</th><th>Línea</th><th>Columna</th></tr>");
            
            for (int i = 0; i < tokens.size(); i++) {
                TokenEntry t = tokens.get(i);
                sb.append("<tr>");
                sb.append("<td>").append(i + 1).append("</td>");
                sb.append("<td>").append(t.lexema).append("</td>");
                sb.append("<td>").append(t.tipo).append("</td>");
                sb.append("<td>").append(t.linea).append("</td>");
                sb.append("<td>").append(t.columna).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        
        sb.append("</body></html>");
        return sb.toString();
    }
    
    // Clase interna para tokens
    public static class TokenEntry {
        public String lexema;
        public String tipo;
        public int linea;
        public int columna;
        
        public TokenEntry(String lexema, String tipo, int linea, int columna) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.linea = linea;
            this.columna = columna;
        }
    }
}