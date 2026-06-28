package olc1.golite.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// Generador de reportes en formato HTML.
// Lo diseñé para renderizar tablas dinámicas y limpias con tokens, errores y la tabla de símbolos del programa.
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

    public static File generateAstReport(File outputFile, String astJson) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print("<!DOCTYPE html>\n");
            writer.print("<html lang=\"es\">\n<head>\n");
            writer.print("    <meta charset=\"UTF-8\">\n");
            writer.print("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.print("    <title>Reporte de AST</title>\n");
            writer.print("    <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap\" rel=\"stylesheet\">\n");
            writer.print("    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/d3/7.9.0/d3.min.js\"></script>\n");
            writer.print("    <style>\n");
            writer.print("        :root {\n");
            writer.print("            --bg-color: #0b0f19;\n");
            writer.print("            --card-bg: #151b2c;\n");
            writer.print("            --text-main: #f3f4f6;\n");
            writer.print("            --text-muted: #9ca3af;\n");
            writer.print("            --accent: #6366f1;\n");
            writer.print("            --accent2: #a855f7;\n");
            writer.print("            --border-color: #242f47;\n");
            writer.print("        }\n");
            writer.print("        * { box-sizing: border-box; margin: 0; padding: 0; }\n");
            writer.print("        body {\n");
            writer.print("            font-family: 'Plus Jakarta Sans', sans-serif;\n");
            writer.print("            background-color: var(--bg-color);\n");
            writer.print("            color: var(--text-main);\n");
            writer.print("            padding: 2.5rem 1.5rem;\n");
            writer.print("            min-height: 100vh;\n");
            writer.print("        }\n");
            writer.print("        .container { max-width: 1400px; margin: 0 auto; }\n");
            writer.print("        header { margin-bottom: 2rem; text-align: center; }\n");
            writer.print("        h1 {\n");
            writer.print("            font-size: 2.25rem; font-weight: 700;\n");
            writer.print("            background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);\n");
            writer.print("            -webkit-background-clip: text; -webkit-text-fill-color: transparent;\n");
            writer.print("            margin-bottom: 0.5rem;\n");
            writer.print("        }\n");
            writer.print("        .subtitle { color: var(--text-muted); font-size: 1rem; }\n");
            writer.print("        .hint { color: var(--text-muted); font-size: 0.85rem; margin-top: 0.5rem; }\n");
            writer.print("        .card {\n");
            writer.print("            background-color: var(--card-bg);\n");
            writer.print("            border-radius: 16px;\n");
            writer.print("            border: 1px solid var(--border-color);\n");
            writer.print("            box-shadow: 0 10px 25px -5px rgba(0,0,0,0.3);\n");
            writer.print("            overflow: hidden;\n");
            writer.print("            padding: 1.5rem;\n");
            writer.print("        }\n");
            writer.print("        #tree-container {\n");
            writer.print("            width: 100%; overflow: auto;\n");
            writer.print("            background: #0e1420;\n");
            writer.print("            border-radius: 12px;\n");
            writer.print("            border: 1px solid var(--border-color);\n");
            writer.print("            min-height: 500px;\n");
            writer.print("        }\n");
            writer.print("        svg { display: block; }\n");
            writer.print("        .node circle {\n");
            writer.print("            fill: #1e293b;\n");
            writer.print("            stroke: #6366f1;\n");
            writer.print("            stroke-width: 2px;\n");
            writer.print("            cursor: pointer;\n");
            writer.print("            transition: fill 0.2s, r 0.2s;\n");
            writer.print("        }\n");
            writer.print("        .node circle:hover { fill: #6366f1; }\n");
            writer.print("        .node.collapsed circle { fill: #a855f7; stroke: #a855f7; }\n");
            writer.print("        .node text {\n");
            writer.print("            font-family: 'Plus Jakarta Sans', monospace;\n");
            writer.print("            font-size: 11px;\n");
            writer.print("            fill: #f3f4f6;\n");
            writer.print("            pointer-events: none;\n");
            writer.print("        }\n");
            writer.print("        .link {\n");
            writer.print("            fill: none;\n");
            writer.print("            stroke: #2d3f5e;\n");
            writer.print("            stroke-width: 1.5px;\n");
            writer.print("        }\n");
            writer.print("        .controls {\n");
            writer.print("            display: flex; gap: 0.75rem; margin-bottom: 1rem; flex-wrap: wrap;\n");
            writer.print("        }\n");
            writer.print("        .btn {\n");
            writer.print("            padding: 0.4rem 1rem;\n");
            writer.print("            border-radius: 8px;\n");
            writer.print("            border: 1px solid var(--border-color);\n");
            writer.print("            background: #1e293b;\n");
            writer.print("            color: var(--text-main);\n");
            writer.print("            font-family: inherit;\n");
            writer.print("            font-size: 0.85rem;\n");
            writer.print("            cursor: pointer;\n");
            writer.print("            transition: background 0.2s;\n");
            writer.print("        }\n");
            writer.print("        .btn:hover { background: #6366f1; }\n");
            writer.print("    </style>\n");
            writer.print("</head>\n<body>\n");
            writer.print("    <div class=\"container\">\n");
            writer.print("        <header>\n");
            writer.print("            <h1>Reporte de AST</h1>\n");
            writer.print("            <p class=\"subtitle\">Golite Compiler - Reportes Generados Automáticamente</p>\n");
            writer.print("            <p class=\"hint\">Haz clic en un nodo para colapsar/expandir sus hijos. Usa la rueda del mouse para hacer zoom.</p>\n");
            writer.print("        </header>\n");
            writer.print("        <div class=\"card\">\n");
            writer.print("            <div class=\"controls\">\n");
            writer.print("                <button class=\"btn\" onclick=\"expandAll()\">Expandir todo</button>\n");
            writer.print("                <button class=\"btn\" onclick=\"collapseAll()\">Colapsar todo</button>\n");
            writer.print("                <button class=\"btn\" onclick=\"resetView()\">Centrar vista</button>\n");
            writer.print("            </div>\n");
            writer.print("            <div id=\"tree-container\"></div>\n");
            writer.print("        </div>\n");
            writer.print("    </div>\n");

            // Embed AST data and D3 rendering script
            writer.print("    <script>\n");
            writer.print("    const astData = " + astJson + ";\n");
            writer.print("    const margin = {top: 30, right: 120, bottom: 30, left: 60};\n");
            writer.print("    const width  = 1300 - margin.left - margin.right;\n");
            writer.print("    const height = 800  - margin.top  - margin.bottom;\n");
            writer.print("    let i = 0, duration = 250;\n\n");
            writer.print("    const svgRoot = d3.select('#tree-container').append('svg')\n");
            writer.print("        .attr('width',  width  + margin.left + margin.right)\n");
            writer.print("        .attr('height', height + margin.top  + margin.bottom)\n");
            writer.print("        .call(d3.zoom().scaleExtent([0.1, 4]).on('zoom', e => g.attr('transform', e.transform)))\n");
            writer.print("        .on('dblclick.zoom', null);\n\n");
            writer.print("    const g = svgRoot.append('g').attr('transform',`translate(${margin.left},${margin.top})`);\n\n");
            writer.print("    const treeFn = d3.tree().size([height, width]);\n");
            writer.print("    const root   = d3.hierarchy(astData, d => d.children);\n");
            writer.print("    root.x0 = height / 2; root.y0 = 0;\n\n");
            writer.print("    // Collapse all nodes except root at startup\n");
            writer.print("    if (root.children) { root.children.forEach(collapseNode); }\n\n");
            writer.print("    update(root);\n\n");
            writer.print("    function collapseNode(d) {\n");
            writer.print("        if (d.children) {\n");
            writer.print("            d._children = d.children;\n");
            writer.print("            d._children.forEach(collapseNode);\n");
            writer.print("            d.children = null;\n");
            writer.print("        }\n");
            writer.print("    }\n\n");
            writer.print("    function expandNode(d) {\n");
            writer.print("        if (d._children) {\n");
            writer.print("            d.children = d._children;\n");
            writer.print("            d._children = null;\n");
            writer.print("        }\n");
            writer.print("        if (d.children) { d.children.forEach(expandNode); }\n");
            writer.print("    }\n\n");
            writer.print("    function expandAll()  { expandNode(root); update(root); }\n");
            writer.print("    function collapseAll() {\n");
            writer.print("        if (root.children) { root.children.forEach(collapseNode); }\n");
            writer.print("        update(root);\n");
            writer.print("    }\n");
            writer.print("    function resetView() {\n");
            writer.print("        svgRoot.transition().duration(400).call(\n");
            writer.print("            d3.zoom().transform, d3.zoomIdentity.translate(margin.left, margin.top));\n");
            writer.print("    }\n\n");
            writer.print("    function update(source) {\n");
            writer.print("        const treeData = treeFn(root);\n");
            writer.print("        const nodes    = treeData.descendants();\n");
            writer.print("        const links    = treeData.descendants().slice(1);\n");
            writer.print("        nodes.forEach(d => { d.y = d.depth * 200; });\n\n");
            // Nodes
            writer.print("        const node = g.selectAll('g.node').data(nodes, d => d.id || (d.id = ++i));\n");
            writer.print("        const nodeEnter = node.enter().append('g')\n");
            writer.print("            .attr('class', d => 'node' + (d._children ? ' collapsed' : ''))\n");
            writer.print("            .attr('transform', d => `translate(${source.y0},${source.x0})`)\n");
            writer.print("            .on('click', (event, d) => { click(d); update(d); });\n\n");
            writer.print("        nodeEnter.append('circle').attr('r', 1e-6);\n");
            writer.print("        nodeEnter.append('text')\n");
            writer.print("            .attr('dy', '.35em')\n");
            writer.print("            .attr('x', d => d.children || d._children ? -14 : 14)\n");
            writer.print("            .attr('text-anchor', d => d.children || d._children ? 'end' : 'start')\n");
            writer.print("            .text(d => d.data.name);\n\n");
            writer.print("        const nodeUpdate = nodeEnter.merge(node);\n");
            writer.print("        nodeUpdate.transition().duration(duration)\n");
            writer.print("            .attr('transform', d => `translate(${d.y},${d.x})`)\n");
            writer.print("            .attr('class', d => 'node' + (d._children ? ' collapsed' : ''));\n");
            writer.print("        nodeUpdate.select('circle').transition().duration(duration).attr('r', 8);\n\n");
            writer.print("        const nodeExit = node.exit().transition().duration(duration)\n");
            writer.print("            .attr('transform', d => `translate(${source.y},${source.x})`).remove();\n");
            writer.print("        nodeExit.select('circle').attr('r', 1e-6);\n\n");
            // Links
            writer.print("        const link = g.selectAll('path.link').data(links, d => d.id);\n");
            writer.print("        const linkEnter = link.enter().insert('path', 'g')\n");
            writer.print("            .attr('class', 'link')\n");
            writer.print("            .attr('d', d => { const o={x:source.x0,y:source.y0}; return diagonal(o,o); });\n\n");
            writer.print("        linkEnter.merge(link).transition().duration(duration)\n");
            writer.print("            .attr('d', d => diagonal(d, d.parent));\n\n");
            writer.print("        link.exit().transition().duration(duration)\n");
            writer.print("            .attr('d', d => { const o={x:source.x,y:source.y}; return diagonal(o,o); }).remove();\n\n");
            writer.print("        nodes.forEach(d => { d.x0 = d.x; d.y0 = d.y; });\n");
            writer.print("    }\n\n");
            writer.print("    function diagonal(s, d) {\n");
            writer.print("        return `M${s.y},${s.x}C${(s.y+d.y)/2},${s.x} ${(s.y+d.y)/2},${d.x} ${d.y},${d.x}`;\n");
            writer.print("    }\n\n");
            writer.print("    function click(d) {\n");
            writer.print("        if (d.children) { d._children = d.children; d.children = null; }\n");
            writer.print("        else             { d.children = d._children; d._children = null; }\n");
            writer.print("    }\n");
            writer.print("    </script>\n");
            writer.print("</body>\n</html>");
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
