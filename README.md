# GoLite - Intérprete del lenguaje GoLite

##  Descripción General

Proyecto desarrollado para el curso de **Organización de Lenguajes y Compiladores 1** (OLC1) de la Universidad San Carlos de Guatemala.

Este proyecto implementa un **intérprete** para el lenguaje de programación **GoLite**, un subconjunto del lenguaje Go adaptado para fines educativos. El intérprete incluye:

- Análisis léxico generado con **JFLEX**
- Análisis sintáctico generado con **CUP**
- Construcción de Árbol de Sintaxis Abstracta (AST)
- Interpretación mediante el **patrón Visitor**
- Manejo de ámbitos y tabla de símbolos
- Sistema de tipos estáticos con conversión implícita `int → float64`
- Interfaz gráfica con editor y consola

## 🛠️ Tecnologías utilizadas

| Tecnología | Versión | Uso           |
|------------|---------|---------------|
| Java       | 21      | Lenguaje base |
| JFLEX      | 1.9.1   | Analizador léxico |
| CUP        | 11b     | Analizador sintáctico |
| Maven      | 3.8+    | Gestión de dependencias |
| RSyntaxTextArea | 3.3.4 | Editor con resaltado de sintaxis |

##  Funcionalidades implementadas (Fase 1)

### Análisis Léxico
- Identificadores (case sensitive)
- Comentarios de una línea `//` y multilínea `/* */`
- Tipos primitivos: `int`, `float64`, `string`, `bool`
- Operadores aritméticos (`+`, `-`, `*`, `/`, `%`)
- Operadores relacionales (`<`, `>`, `<=`, `>=`, `==`, `!=`)
- Operadores lógicos (`&&`, `||`, `!`)
- Palabras reservadas (`if`, `else`, `for`, `break`, `print`, `true`, `false`)

### Análisis Sintáctico y Semántico
- Declaración de variables (`var id tipo` e `id := expresion`)
- Asignación (`id = expresion`)
- Condicional `if-else if-else`
- Bucle `for` (tipo while)
- Sentencia `break`
- Función `main` como punto de entrada
- Tipado estático (no reasignación de tipos)
- Conversión implícita `int → float64`
- Manejo de ámbitos (bloques `{ }`)

### Funciones embebidas
- `fmt.Println()` - Imprimir en consola
- `strconv.Atoi()` - Convertir string a int
- `strconv.ParseFloat()` - Convertir string a float64
- `reflect.TypeOf().string` - Obtener tipo de variable

### Reportes
- Reporte de errores (léxicos, sintácticos, semánticos)
- Tabla de tokens

### Interfaz Gráfica
- Editor con resaltado de sintaxis (estilo Go)
- Área de consola
- Botones: Ejecutar, Limpiar consola
- Menú Archivo: Nuevo, Abrir, Guardar, Salir
- Menú Reportes: Reporte de errores, Tabla de tokens
- Barra de estado (línea y columna)

##  Instrucciones de ejecución

### Requisitos previos

Antes de ejecutar el programa, asegúrese de tener instalado:

```bash
# Verificar versión de Java
java -version
# Debe mostrar Java 21 o superior

# Verificar Maven
mvn -version
# Debe mostrar Maven 3.8 o superior