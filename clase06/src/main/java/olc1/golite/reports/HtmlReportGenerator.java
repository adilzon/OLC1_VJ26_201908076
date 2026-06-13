package olc1.golite.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class HtmlReportGenerator {

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#039;");
    }

    private static String getHtmlHeader(String title) {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"es\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>" + title + "</title>\n" +
               "    <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap\" rel=\"stylesheet\">\n" +
               "    <style>\n" +
               "        :root {\n" +
               "            --bg-color: #0b0f19;\n" +
               "            --card-bg: #151b2c;\n" +
               "            --text-main: #f3f4f6;\n" +
               "            --text-muted: #9ca3af;\n" +
               "            --accent: #6366f1;\n" +
               "            --accent-gradient: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);\n" +
               "            --table-hdr: #1e293b;\n" +
               "            --border-color: #242f47;\n" +
               "            --row-hover: #1e263d;\n" +
               "        }\n" +
               "        * {\n" +
               "            box-sizing: border-box;\n" +
               "            margin: 0;\n" +
               "            padding: 0;\n" +
               "        }\n" +
               "        body {\n" +
               "            font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
               "            background-color: var(--bg-color);\n" +
               "            color: var(--text-main);\n" +
               "            padding: 2.5rem 1.5rem;\n" +
               "            min-height: 100vh;\n" +
               "        }\n" +
               "        .container {\n" +
               "            max-width: 1100px;\n" +
               "            margin: 0 auto;\n" +
               "        }\n" +
               "        header {\n" +
               "            margin-bottom: 2.5rem;\n" +
               "            text-align: center;\n" +
               "        }\n" +
               "        h1 {\n" +
               "            font-size: 2.25rem;\n" +
               "            font-weight: 700;\n" +
               "            background: var(--accent-gradient);\n" +
               "            -webkit-background-clip: text;\n" +
               "            -webkit-text-fill-color: transparent;\n" +
               "            margin-bottom: 0.5rem;\n" +
               "        }\n" +
               "        .subtitle {\n" +
               "            color: var(--text-muted);\n" +
               "            font-size: 1rem;\n" +
               "        }\n" +
               "        .card {\n" +
               "            background-color: var(--card-bg);\n" +
               "            border-radius: 16px;\n" +
               "            border: 1px solid var(--border-color);\n" +
               "            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3), 0 8px 10px -6px rgba(0, 0, 0, 0.3);\n" +
               "            overflow: hidden;\n" +
               "        }\n" +
               "        .table-responsive {\n" +
               "            width: 100%;\n" +
               "            overflow-x: auto;\n" +
               "        }\n" +
               "        table {\n" +
               "            width: 100%;\n" +
               "            border-collapse: collapse;\n" +
               "            text-align: left;\n" +
               "            font-size: 0.95rem;\n" +
               "        }\n" +
               "        th {\n" +
               "            background-color: var(--table-hdr);\n" +
               "            color: var(--text-main);\n" +
               "            font-weight: 600;\n" +
               "            padding: 1rem 1.25rem;\n" +
               "            border-bottom: 1px solid var(--border-color);\n" +
               "            text-transform: uppercase;\n" +
               "            font-size: 0.75rem;\n" +
               "            letter-spacing: 0.05em;\n" +
               "        }\n" +
               "        td {\n" +
               "            padding: 1rem 1.25rem;\n" +
               "            border-bottom: 1px solid var(--border-color);\n" +
               "            color: var(--text-main);\n" +
               "            transition: background-color 0.2s;\n" +
               "        }\n" +
               "        tr:last-child td {\n" +
               "            border-bottom: none;\n" +
               "        }\n" +
               "        tr:hover td {\n" +
               "            background-color: var(--row-hover);\n" +
               "        }\n" +
               "        .badge {\n" +
               "            display: inline-flex;\n" +
               "            align-items: center;\n" +
               "            padding: 0.25rem 0.75rem;\n" +
               "            border-radius: 9999px;\n" +
               "            font-size: 0.75rem;\n" +
               "            font-weight: 600;\n" +
               "        }\n" +
               "        .badge-lexico {\n" +
               "            background-color: rgba(239, 68, 68, 0.15);\n" +
               "            color: #ef4444;\n" +
               "            border: 1px solid rgba(239, 68, 68, 0.2);\n" +
               "        }\n" +
               "        .badge-sintactico {\n" +
               "            background-color: rgba(245, 158, 11, 0.15);\n" +
               "            color: #f59e0b;\n" +
               "            border: 1px solid rgba(245, 158, 11, 0.2);\n" +
               "        }\n" +
               "        .badge-semantico {\n" +
               "            background-color: rgba(16, 185, 129, 0.15);\n" +
               "            color: #10b981;\n" +
               "            border: 1px solid rgba(16, 185, 129, 0.2);\n" +
               "        }\n" +
               "        .badge-token {\n" +
               "            background-color: rgba(99, 102, 241, 0.15);\n" +
               "            color: #818cf8;\n" +
               "            border: 1px solid rgba(99, 102, 241, 0.2);\n" +
               "        }\n" +
               "        .badge-symbol {\n" +
               "            background-color: rgba(168, 85, 247, 0.15);\n" +
               "            color: #c084fc;\n" +
               "            border: 1px solid rgba(168, 85, 247, 0.2);\n" +
               "        }\n" +
               "        .empty-state {\n" +
               "            padding: 4rem 2rem;\n" +
               "            text-align: center;\n" +
               "            color: var(--text-muted);\n" +
               "            font-size: 1.1rem;\n" +
               "        }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <header>\n" +
               "            <h1>" + title + "</h1>\n" +
               "            <p class=\"subtitle\">Golite Compiler - Reportes Generados Automáticamente</p>\n" +
               "        </header>\n" +
               "        <div class=\"card\">\n";
    }

    private static String getHtmlFooter() {
        return "        </div>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }

    public static File generateErrorsReport(File outputFile, List<GoliteError> errors) throws IOException {
        // Asegurar que exista la carpeta contenedora
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print(getHtmlHeader("Reporte de Errores"));

            if (errors == null || errors.isEmpty()) {
                writer.print("            <div class=\"empty-state\">\n");
                writer.print("                <p>🎉 ¡Felicidades! No se encontraron errores léxicos, sintácticos o semánticos.</p>\n");
                writer.print("            </div>\n");
            } else {
                writer.print("            <div class=\"table-responsive\">\n");
                writer.print("                <table>\n");
                writer.print("                    <thead>\n");
                writer.print("                        <tr>\n");
                writer.print("                            <th>#</th>\n");
                writer.print("                            <th>Tipo de Error</th>\n");
                writer.print("                            <th>Descripción</th>\n");
                writer.print("                            <th>Línea</th>\n");
                writer.print("                            <th>Columna</th>\n");
                writer.print("                        </tr>\n");
                writer.print("                    </thead>\n");
                writer.print("                    <tbody>\n");

                int index = 1;
                for (GoliteError err : errors) {
                    String badgeClass = "badge-semantico";
                    String type = err.getType();
                    if ("Lexico".equalsIgnoreCase(type)) {
                        badgeClass = "badge-lexico";
                    } else if ("Sintactico".equalsIgnoreCase(type)) {
                        badgeClass = "badge-sintactico";
                    }

                    writer.print("                        <tr>\n");
                    writer.print("                            <td>" + index++ + "</td>\n");
                    writer.print("                            <td><span class=\"badge " + badgeClass + "\">" + escapeHtml(type) + "</span></td>\n");
                    writer.print("                            <td>" + escapeHtml(err.getDescription()) + "</td>\n");
                    writer.print("                            <td>" + err.getLine() + "</td>\n");
                    writer.print("                            <td>" + err.getColumn() + "</td>\n");
                    writer.print("                        </tr>\n");
                }

                writer.print("                    </tbody>\n");
                writer.print("                </table>\n");
                writer.print("            </div>\n");
            }

            writer.print(getHtmlFooter());
        }
        return outputFile;
    }

    public static File generateTokensReport(File outputFile, List<Token> tokens) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print(getHtmlHeader("Reporte de Tokens"));

            if (tokens == null || tokens.isEmpty()) {
                writer.print("            <div class=\"empty-state\">\n");
                writer.print("                <p>No se encontraron tokens escaneados.</p>\n");
                writer.print("            </div>\n");
            } else {
                writer.print("            <div class=\"table-responsive\">\n");
                writer.print("                <table>\n");
                writer.print("                    <thead>\n");
                writer.print("                        <tr>\n");
                writer.print("                            <th>#</th>\n");
                writer.print("                            <th>Tipo de Token</th>\n");
                writer.print("                            <th>Lexema</th>\n");
                writer.print("                            <th>Línea</th>\n");
                writer.print("                            <th>Columna</th>\n");
                writer.print("                        </tr>\n");
                writer.print("                    </thead>\n");
                writer.print("                    <tbody>\n");

                int index = 1;
                for (Token tok : tokens) {
                    writer.print("                        <tr>\n");
                    writer.print("                            <td>" + index++ + "</td>\n");
                    writer.print("                            <td><span class=\"badge badge-token\">" + escapeHtml(tok.getType()) + "</span></td>\n");
                    writer.print("                            <td><code>" + escapeHtml(tok.getLexeme()) + "</code></td>\n");
                    writer.print("                            <td>" + tok.getLine() + "</td>\n");
                    writer.print("                            <td>" + tok.getColumn() + "</td>\n");
                    writer.print("                        </tr>\n");
                }

                writer.print("                    </tbody>\n");
                writer.print("                </table>\n");
                writer.print("            </div>\n");
            }

            writer.print(getHtmlFooter());
        }
        return outputFile;
    }

    public static File generateSymbolsReport(File outputFile, List<Symbol> symbols) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print(getHtmlHeader("Tabla de Símbolos"));

            if (symbols == null || symbols.isEmpty()) {
                writer.print("            <div class=\"empty-state\">\n");
                writer.print("                <p>No se declararon variables o símbolos durante la ejecución.</p>\n");
                writer.print("            </div>\n");
            } else {
                writer.print("            <div class=\"table-responsive\">\n");
                writer.print("                <table>\n");
                writer.print("                    <thead>\n");
                writer.print("                        <tr>\n");
                writer.print("                            <th>#</th>\n");
                writer.print("                            <th>Nombre</th>\n");
                writer.print("                            <th>Tipo de Símbolo</th>\n");
                writer.print("                            <th>Tipo de Dato</th>\n");
                writer.print("                            <th>Ámbito / Entorno</th>\n");
                writer.print("                            <th>Línea</th>\n");
                writer.print("                            <th>Columna</th>\n");
                writer.print("                        </tr>\n");
                writer.print("                    </thead>\n");
                writer.print("                    <tbody>\n");

                int index = 1;
                for (Symbol sym : symbols) {
                    writer.print("                        <tr>\n");
                    writer.print("                            <td>" + index++ + "</td>\n");
                    writer.print("                            <td><strong>" + escapeHtml(sym.getName()) + "</strong></td>\n");
                    writer.print("                            <td><span class=\"badge badge-symbol\">" + escapeHtml(sym.getSymbolType()) + "</span></td>\n");
                    writer.print("                            <td><code>" + escapeHtml(sym.getDataType()) + "</code></td>\n");
                    writer.print("                            <td>" + escapeHtml(sym.getScope()) + "</td>\n");
                    writer.print("                            <td>" + sym.getLine() + "</td>\n");
                    writer.print("                            <td>" + sym.getColumn() + "</td>\n");
                    writer.print("                        </tr>\n");
                }

                writer.print("                    </tbody>\n");
                writer.print("                </table>\n");
                writer.print("            </div>\n");
            }

            writer.print(getHtmlFooter());
        }
        return outputFile;
    }
}
