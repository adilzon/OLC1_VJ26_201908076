package golite.interpreter;

import golite.ast.Nodo;
import golite.errors.ErrorHandler;
import golite.errors.ErrorHandler.TokenEntry;
import java.util.ArrayList;
import java.util.HashMap;

public class Interprete {

    // Salida del programa (lo que imprime fmt.Println)
    private StringBuilder salida = new StringBuilder();

    // Tabla de símbolos: guarda variables por ámbito
    private ArrayList<HashMap<String, Object[]>> ambitos = new ArrayList<>();
    
    // Tabla de funciones declaradas
    private HashMap<String, Nodo> funciones = new HashMap<>();

    // Señales de control de flujo
    private boolean señalBreak = false;
    private boolean señalContinue = false;
    private boolean señalReturn = false;
    private Object valorReturn = null;

    public Interprete() {
        // Crear ámbito global
        ambitos.add(new HashMap<>());
    }

    // ========== ÁMBITOS ==========
    private void entrarAmbito() {
        ambitos.add(new HashMap<>());
    }

    private void salirAmbito() {
        if (ambitos.size() > 1) {
            ambitos.remove(ambitos.size() - 1);
        }
    }

    private HashMap<String, Object[]> ambitoActual() {
        return ambitos.get(ambitos.size() - 1);
    }

    // Buscar variable en todos los ámbitos (del más interno al más externo)
    private Object[] buscarVariable(String nombre) {
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            if (ambitos.get(i).containsKey(nombre)) {
                return ambitos.get(i).get(nombre);
            }
        }
        return null;
    }

    // Actualizar variable en el ámbito donde existe
    private boolean actualizarVariable(String nombre, Object valor) {
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            if (ambitos.get(i).containsKey(nombre)) {
                Object[] entrada = ambitos.get(i).get(nombre);
                entrada[1] = valor;
                return true;
            }
        }
        return false;
    }

    // ========== PUNTO DE ENTRADA ==========
    public String ejecutar(Nodo raiz) {
    salida = new StringBuilder();

    // Reiniciar ámbitos
    ambitos.clear();
    ambitos.add(new HashMap<>());

    // Limpiar funciones
    funciones.clear();

    // Reiniciar señales
    señalBreak = false;
    señalContinue = false;
    señalReturn = false;
    valorReturn = null;

    if (raiz == null) {
        return "Error: programa vacío";
    }

    // DEBUG
    System.out.println("RAIZ: " + raiz.tipo);
    System.out.println("HIJOS RAIZ: " + raiz.hijos.size());

    for (int i = 0; i < raiz.hijos.size(); i++) {
        Nodo hijo = raiz.hijos.get(i);

        System.out.println(
            "HIJO " + i +
            " -> " + hijo.tipo +
            " hijos=" + hijo.hijos.size()
        );

        ejecutarSentencia(hijo);

        // Si es lista de sentencias
        if (hijo.tipo.equals("lista_sentencias")
                || hijo.tipo.equals("lista")) {

            ejecutarLista(hijo.hijos);
        }
    }

    System.out.println("SALIDA FINAL: " + salida);

    return salida.toString();
}

    // ========== EJECUTAR LISTA DE SENTENCIAS ==========
    private void ejecutarLista(ArrayList<Nodo> sentencias) {
        for (Nodo s : sentencias) {
            if (señalBreak || señalContinue || señalReturn) break;
            ejecutarSentencia(s);
        }
    }

    // ========== EJECUTAR SENTENCIA ==========
    private void ejecutarSentencia(Nodo nodo) {
        if (nodo == null) return;

        switch (nodo.tipo) {
            case "var_decl":        ejecutarVarDecl(nodo); break;
            case "var_decl_sin_valor": ejecutarVarDeclSinValor(nodo); break;
            case "var_inferida":    ejecutarVarInferida(nodo); break;
            case "asignacion":      ejecutarAsignacion(nodo); break;
            case "mas_igual":       ejecutarMasIgual(nodo); break;
            case "menos_igual":     ejecutarMenosIgual(nodo); break;
            case "masmas":          ejecutarMasMas(nodo); break;
            case "menosmenos":      ejecutarMenosMenos(nodo); break;
            case "if":              ejecutarIf(nodo); break;
            case "if_else":         ejecutarIfElse(nodo); break;
            case "if_else_if":      ejecutarIfElseIf(nodo); break;
            case "for_while":       ejecutarForWhile(nodo); break;
            case "for_clasico":     ejecutarForClasico(nodo); break;
            case "for_clasico_masmas": ejecutarForClasicoMasMas(nodo); break;
            case "for_clasico_menosmenos": ejecutarForClasicoMenosMenos(nodo); break;
            case "println":         ejecutarPrintln(nodo); break;
            case "bloque":          ejecutarBloque(nodo); break;
            case "break":           señalBreak = true; break;
            case "continue":        señalContinue = true; break;
            case "funcion":
            case "funcion_void":    
                funciones.put((String) nodo.valor, nodo); break;
            case "llamada":         evaluarExpresion(nodo); break;
            default: break;
        }
    }

    // ========== VARIABLES ==========
    private void ejecutarVarDecl(Nodo nodo) {
        String nombre = (String) nodo.valor;
        String tipo = (String) nodo.hijos.get(0).valor;
        Object valor = evaluarExpresion(nodo.hijos.get(1));
        valor = convertirTipo(valor, tipo, nodo);
        ambitoActual().put(nombre, new Object[]{tipo, valor});
    }

    private void ejecutarVarDeclSinValor(Nodo nodo) {
        String nombre = (String) nodo.valor;
        String tipo = (String) nodo.hijos.get(0).valor;
        Object valorDefault = valorPorDefecto(tipo);
        ambitoActual().put(nombre, new Object[]{tipo, valorDefault});
    }

    private void ejecutarVarInferida(Nodo nodo) {
        String nombre = (String) nodo.valor;
        Object valor = evaluarExpresion(nodo.hijos.get(0));
        String tipo = inferirTipo(valor);
        ambitoActual().put(nombre, new Object[]{tipo, valor});
    }

    private void ejecutarAsignacion(Nodo nodo) {
        String nombre = (String) nodo.valor;
        Object[] entrada = buscarVariable(nombre);
        if (entrada == null) {
            ErrorHandler.errorSemantico("Variable '" + nombre + "' no declarada", 0, 0);
            salida.append("[ERROR] Variable '").append(nombre).append("' no declarada\n");
            return;
        }
        Object nuevoValor = evaluarExpresion(nodo.hijos.get(0));
        nuevoValor = convertirTipo(nuevoValor, (String) entrada[0], nodo);
        actualizarVariable(nombre, nuevoValor);
    }

    private void ejecutarMasIgual(Nodo nodo) {
        String nombre = (String) nodo.valor;
        Object[] entrada = buscarVariable(nombre);
        if (entrada == null) return;
        Object derecha = evaluarExpresion(nodo.hijos.get(0));
        Object resultado = operarSuma(entrada[1], derecha);
        actualizarVariable(nombre, resultado);
    }

    private void ejecutarMenosIgual(Nodo nodo) {
        String nombre = (String) nodo.valor;
        Object[] entrada = buscarVariable(nombre);
        if (entrada == null) return;
        Object derecha = evaluarExpresion(nodo.hijos.get(0));
        Object resultado = operarResta(entrada[1], derecha);
        actualizarVariable(nombre, resultado);
    }

    private void ejecutarMasMas(Nodo nodo) {
        String nombre = (String) nodo.valor;
        Object[] entrada = buscarVariable(nombre);
        if (entrada == null) return;
        if (entrada[1] instanceof Integer) {
            actualizarVariable(nombre, (Integer) entrada[1] + 1);
        } else if (entrada[1] instanceof Double) {
            actualizarVariable(nombre, (Double) entrada[1] + 1.0);
        }
    }

    private void ejecutarMenosMenos(Nodo nodo) {
        String nombre = (String) nodo.valor;
        Object[] entrada = buscarVariable(nombre);
        if (entrada == null) return;
        if (entrada[1] instanceof Integer) {
            actualizarVariable(nombre, (Integer) entrada[1] - 1);
        } else if (entrada[1] instanceof Double) {
            actualizarVariable(nombre, (Double) entrada[1] - 1.0);
        }
    }

    // ========== IF / ELSE ==========
    private void ejecutarIf(Nodo nodo) {
        Object cond = evaluarExpresion(nodo.hijos.get(0));
        if (esVerdadero(cond)) {
            ejecutarBloque(nodo.hijos.get(1));
        }
    }

    private void ejecutarIfElse(Nodo nodo) {
        Object cond = evaluarExpresion(nodo.hijos.get(0));
        if (esVerdadero(cond)) {
            ejecutarBloque(nodo.hijos.get(1));
        } else {
            ejecutarBloque(nodo.hijos.get(2));
        }
    }

    private void ejecutarIfElseIf(Nodo nodo) {
        Object cond = evaluarExpresion(nodo.hijos.get(0));
        if (esVerdadero(cond)) {
            ejecutarBloque(nodo.hijos.get(1));
        } else {
            ejecutarSentencia(nodo.hijos.get(2));
        }
    }

    // ========== FOR ==========
    private void ejecutarForWhile(Nodo nodo) {
        entrarAmbito();
        while (true) {
            Object cond = evaluarExpresion(nodo.hijos.get(0));
            if (!esVerdadero(cond)) break;
            ejecutarBloque(nodo.hijos.get(1));
            if (señalBreak) { señalBreak = false; break; }
            if (señalContinue) { señalContinue = false; continue; }
            if (señalReturn) break;
        }
        salirAmbito();
    }

    private void ejecutarForClasico(Nodo nodo) {
        entrarAmbito();
        ejecutarSentencia(nodo.hijos.get(0)); // inicialización
        while (true) {
            Object cond = evaluarExpresion(nodo.hijos.get(1));
            if (!esVerdadero(cond)) break;
            ejecutarBloque(nodo.hijos.get(3));
            if (señalBreak) { señalBreak = false; break; }
            if (señalContinue) { señalContinue = false; }
            if (señalReturn) break;
            ejecutarSentencia(nodo.hijos.get(2)); // incremento
        }
        salirAmbito();
    }

    private void ejecutarForClasicoMasMas(Nodo nodo) {
        entrarAmbito();
        ejecutarSentencia(nodo.hijos.get(0));
        while (true) {
            Object cond = evaluarExpresion(nodo.hijos.get(1));
            if (!esVerdadero(cond)) break;
            ejecutarBloque(nodo.hijos.get(3));
            if (señalBreak) { señalBreak = false; break; }
            if (señalContinue) { señalContinue = false; }
            if (señalReturn) break;
            ejecutarMasMas(new Nodo("masmas", nodo.hijos.get(2).valor));
        }
        salirAmbito();
    }

    private void ejecutarForClasicoMenosMenos(Nodo nodo) {
        entrarAmbito();
        ejecutarSentencia(nodo.hijos.get(0));
        while (true) {
            Object cond = evaluarExpresion(nodo.hijos.get(1));
            if (!esVerdadero(cond)) break;
            ejecutarBloque(nodo.hijos.get(3));
            if (señalBreak) { señalBreak = false; break; }
            if (señalContinue) { señalContinue = false; }
            if (señalReturn) break;
            ejecutarMenosMenos(new Nodo("menosmenos", nodo.hijos.get(2).valor));
        }
        salirAmbito();
    }

    // ========== BLOQUE ==========
    private void ejecutarBloque(Nodo nodo) {
        entrarAmbito();
        ejecutarLista(nodo.hijos);
        salirAmbito();
    }

    // ========== PRINTLN ==========
    private void ejecutarPrintln(Nodo nodo) {

        if (nodo == null || nodo.hijos.isEmpty()) {
            salida.append("\n");
            return;
        }

        StringBuilder linea = new StringBuilder();

        for (int i = 0; i < nodo.hijos.size(); i++) {

            if (i > 0) {
                linea.append(" ");
            }

            Object valor = evaluarExpresion(nodo.hijos.get(i));

            linea.append(formatearValor(valor));
        }

        salida.append(linea.toString()).append("\n");

        // DEBUG
        System.out.println("PRINTLN -> " + linea);
    }

    // ========== FUNCIONES ==========
    private Object ejecutarFuncion(Nodo nodo, ArrayList<Object> args) {
        entrarAmbito();
        
        // Asignar parámetros si los hay
        if (!nodo.hijos.isEmpty() && nodo.hijos.get(0).tipo.equals("lista")) {
            ArrayList<Nodo> params = nodo.hijos.get(0).hijos;
            for (int i = 0; i < params.size() && i < args.size(); i++) {
                String nombreParam = (String) params.get(i).valor;
                String tipoParam = (String) params.get(i).hijos.get(0).valor;
                ambitoActual().put(nombreParam, new Object[]{tipoParam, args.get(i)});
            }
        }

        // Ejecutar el cuerpo
        Nodo cuerpo = nodo.hijos.get(nodo.hijos.size() - 1);
        ejecutarLista(cuerpo.hijos);

        Object resultado = valorReturn;
        señalReturn = false;
        valorReturn = null;
        salirAmbito();
        return resultado;
    }

    // ========== EVALUAR EXPRESIONES ==========
    public Object evaluarExpresion(Nodo nodo) {
        if (nodo == null) return null;

        switch (nodo.tipo) {
            case "entero":   return nodo.valor;
            case "decimal":  return nodo.valor;
            case "cadena":   return limpiarCadena((String) nodo.valor);
            case "booleano": return nodo.valor;
            case "rune":     return nodo.valor;
            case "nil":      return null;

            case "id": {
                String nombre = (String) nodo.valor;
                Object[] entrada = buscarVariable(nombre);
                if (entrada == null) {
                    ErrorHandler.errorSemantico("Variable '" + nombre + "' no declarada", 0, 0);
                    salida.append("[ERROR] Variable '").append(nombre).append("' no declarada\n");
                    return null;
                }
                return entrada[1];
            }

            case "+": return operarSuma(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case "-": return operarResta(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case "*": return operarMultiplicacion(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case "/": return operarDivision(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case "%": return operarModulo(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));

            case "negacion": {
                Object val = evaluarExpresion(nodo.hijos.get(0));
                if (val instanceof Integer) return -(Integer) val;
                if (val instanceof Double) return -(Double) val;
                return null;
            }

            case "not": {
                Object val = evaluarExpresion(nodo.hijos.get(0));
                if (val instanceof Boolean) return !(Boolean) val;
                return null;
            }

            case "==": return operarIgualdad(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case "!=": {
                Object r = operarIgualdad(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
                return r instanceof Boolean ? !(Boolean) r : null;
            }
            case "<":  return operarMenor(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case ">":  return operarMayor(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case "<=": return operarMenorIgual(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));
            case ">=": return operarMayorIgual(evaluarExpresion(nodo.hijos.get(0)), evaluarExpresion(nodo.hijos.get(1)));

            case "and": {
                Object a = evaluarExpresion(nodo.hijos.get(0));
                Object b = evaluarExpresion(nodo.hijos.get(1));
                if (a instanceof Boolean && b instanceof Boolean)
                    return (Boolean) a && (Boolean) b;
                return false;
            }
            case "or": {
                Object a = evaluarExpresion(nodo.hijos.get(0));
                Object b = evaluarExpresion(nodo.hijos.get(1));
                if (a instanceof Boolean && b instanceof Boolean)
                    return (Boolean) a || (Boolean) b;
                return false;
            }

            case "llamada": {
                String nombre = (String) nodo.valor;
                if (funciones.containsKey(nombre)) {
                    ArrayList<Object> args = new ArrayList<>();
                    if (!nodo.hijos.isEmpty()) {
                        Nodo lista = nodo.hijos.get(0);
                        for (Nodo arg : lista.hijos) {
                            args.add(evaluarExpresion(arg));
                        }
                    }
                    return ejecutarFuncion(funciones.get(nombre), args);
                }
                ErrorHandler.errorSemantico("Función '" + nombre + "' no declarada", 0, 0);
                return null;
            }

            case "atoi": {
                Object val = evaluarExpresion(nodo.hijos.get(0));
                try {
                    return Integer.parseInt(limpiarCadena(val.toString()));
                } catch (Exception e) {
                    ErrorHandler.errorSemantico("strconv.Atoi: no se puede convertir '" + val + "' a int", 0, 0);
                    return 0;
                }
            }

            case "parsefloat": {
                Object val = evaluarExpresion(nodo.hijos.get(0));
                try {
                    return Double.parseDouble(limpiarCadena(val.toString()));
                } catch (Exception e) {
                    ErrorHandler.errorSemantico("strconv.ParseFloat: no se puede convertir '" + val + "' a float64", 0, 0);
                    return 0.0;
                }
            }

            case "typeof": {
                Object val = evaluarExpresion(nodo.hijos.get(0));
                return inferirTipo(val);
            }

            default: return null;
        }
    }

    // ========== OPERACIONES ARITMÉTICAS ==========
    private Object operarSuma(Object a, Object b) {
        if (a instanceof String || b instanceof String) {
            return formatearValor(a) + formatearValor(b);
        }
        if (a instanceof Double || b instanceof Double) {
            return toDouble(a) + toDouble(b);
        }
        if (a instanceof Integer && b instanceof Integer) {
            return (Integer) a + (Integer) b;
        }
        return null;
    }

    private Object operarResta(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) - toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a - (Integer) b;
        return null;
    }

    private Object operarMultiplicacion(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) * toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a * (Integer) b;
        return null;
    }

    private Object operarDivision(Object a, Object b) {
        if (b instanceof Integer && (Integer) b == 0) {
            ErrorHandler.errorSemantico("División entre cero", 0, 0);
            salida.append("[ERROR] División entre cero\n");
            return null;
        }
        if (b instanceof Double && (Double) b == 0.0) {
            ErrorHandler.errorSemantico("División entre cero", 0, 0);
            salida.append("[ERROR] División entre cero\n");
            return null;
        }
        if (a instanceof Double || b instanceof Double) return toDouble(a) / toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a / (Integer) b;
        return null;
    }

    private Object operarModulo(Object a, Object b) {
        if (a instanceof Integer && b instanceof Integer) {
            if ((Integer) b == 0) {
                ErrorHandler.errorSemantico("Módulo entre cero", 0, 0);
                return null;
            }
            return (Integer) a % (Integer) b;
        }
        ErrorHandler.errorSemantico("Módulo solo aplica a enteros", 0, 0);
        return null;
    }

    // ========== COMPARACIONES ==========
    private Object operarIgualdad(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a instanceof Double || b instanceof Double) return toDouble(a).equals(toDouble(b));
        return a.equals(b);
    }

    private Object operarMenor(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) < toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a < (Integer) b;
        if (a instanceof String && b instanceof String) return ((String) a).compareTo((String) b) < 0;
        return false;
    }

    private Object operarMayor(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) > toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a > (Integer) b;
        if (a instanceof String && b instanceof String) return ((String) a).compareTo((String) b) > 0;
        return false;
    }

    private Object operarMenorIgual(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) <= toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a <= (Integer) b;
        return false;
    }

    private Object operarMayorIgual(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) >= toDouble(b);
        if (a instanceof Integer && b instanceof Integer) return (Integer) a >= (Integer) b;
        return false;
    }

    // ========== UTILIDADES ==========
    private Double toDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        if (val instanceof Integer) return ((Integer) val).doubleValue();
        return 0.0;
    }

    private boolean esVerdadero(Object val) {
        if (val instanceof Boolean) return (Boolean) val;
        return false;
    }

    private String inferirTipo(Object val) {
        if (val instanceof Integer) return "int";
        if (val instanceof Double) return "float64";
        if (val instanceof String) return "string";
        if (val instanceof Boolean) return "bool";
        return "nil";
    }

    private Object valorPorDefecto(String tipo) {
        switch (tipo) {
            case "int":    return 0;
            case "float64": return 0.0;
            case "string": return "";
            case "bool":   return false;
            case "rune":   return 0;
            default:       return null;
        }
    }

    private Object convertirTipo(Object valor, String tipo, Nodo nodo) {
        if (valor == null) return valorPorDefecto(tipo);
        switch (tipo) {
            case "int":
                if (valor instanceof Integer) return valor;
                if (valor instanceof Double) {
                    ErrorHandler.errorSemantico("No se puede asignar float64 a int", 0, 0);
                    return valorPorDefecto(tipo);
                }
                return valorPorDefecto(tipo);
            case "float64":
                if (valor instanceof Double) return valor;
                if (valor instanceof Integer) return ((Integer) valor).doubleValue();
                return valorPorDefecto(tipo);
            case "string":
                if (valor instanceof String) return valor;
                ErrorHandler.errorSemantico("Tipo incompatible con string", 0, 0);
                return "";
            case "bool":
                if (valor instanceof Boolean) return valor;
                ErrorHandler.errorSemantico("Tipo incompatible con bool", 0, 0);
                return false;
            default:
                return valor;
        }
    }

    private String limpiarCadena(String s) {
        if (s == null) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        s = s.replace("\\n", "\n")
             .replace("\\t", "\t")
             .replace("\\r", "\r")
             .replace("\\\"", "\"")
             .replace("\\\\", "\\");
        return s;
    }

    private String formatearValor(Object val) {
        if (val == null) return "nil";
        if (val instanceof Double) {
            double d = (Double) val;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        if (val instanceof String) return limpiarCadena((String) val);
        return String.valueOf(val);
    }

    public String getSalida() {
        return salida.toString();
    }
}