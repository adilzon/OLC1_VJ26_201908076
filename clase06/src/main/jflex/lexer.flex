package olc1.golite;

// Importaciones necesarias
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import olc1.golite.reports.GoliteError;
import olc1.golite.reports.Token;

%%

// Configuración de JFLEX
%cup //Indicamos que vamos a usar CUP
// Nombre de la clase del lexer
%class Lexer 
%public // Paquete del lexer
%line // conteo de lienas
%column // conteo de columnas
%unicode // recibir caracteres en formato unicode
// %debug // Habilitar modo debug para ver el proceso de tokenización
%ignorecase // ignorar mayusculas y minusculas

%{
    public final List<GoliteError> errors = new ArrayList<>();
    public final List<Token> tokens = new ArrayList<>();

    private Symbol addToken(int type, String typeName, String lexeme) {
        tokens.add(new Token(typeName, lexeme, yyline, yycolumn));
        return new Symbol(type, yyline, yycolumn, lexeme);
    }
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

%eofval{
    return new Symbol(sym.EOF, yyline, yycolumn, yytext());
%eofval}

// Definición de patrones léxicos
digit = [0-9]
identifier = [a-zA-Z_][a-zA-Z0-9_]*
whitespace = [\ \r\t\f\n]+
escape_char = \\ [\"\\nrt]
normal_char = [^\"\\\n\r]
str_lex = ({normal_char} | {escape_char})*
LineComment = "//" [^\r\n]*
BlockComment = "/*" [^*]* ~"*/"
Comment = {LineComment} | {BlockComment}

%%

// Numbers
{digit}+\.{digit}+  { return addToken(sym.decimal, "decimal", yytext()); }
{digit}+            { return addToken(sym.integer, "integer", yytext()); }

// Symbols (two-character symbols first to avoid prefix matching issues)
":="    { return addToken(sym.assign, "assign", yytext()); }
">="    { return addToken(sym.gte, "gte", yytext()); }
"<="    { return addToken(sym.lte, "lte", yytext()); }
"=="    { return addToken(sym.equal, "equal", yytext()); }
"!="    { return addToken(sym.notequal, "notequal", yytext()); }
"+="    { return addToken(sym.plusAssign, "plusAssign", yytext()); }
"-="    { return addToken(sym.minusAssign, "minusAssign", yytext()); }
"&&"    { return addToken(sym.and, "and", yytext()); }
"||"    { return addToken(sym.or, "or", yytext()); }
"++"    { return addToken(sym.plusPlus, "plusPlus", yytext()); }
"--"    { return addToken(sym.minusMinus, "minusMinus", yytext()); }

// Single character symbols
"("     { return addToken(sym.lparen, "lparen", yytext()); }
")"     { return addToken(sym.rparen, "rparen", yytext()); }
"+"     { return addToken(sym.plus, "plus", yytext()); }
"-"     { return addToken(sym.minus, "minus", yytext()); }
"*"     { return addToken(sym.times, "times", yytext()); }
"/"     { return addToken(sym.slash, "slash", yytext()); }
"="     { return addToken(sym.allocate, "allocate", yytext()); }
";"     { return addToken(sym.scol, "scol", yytext()); }
"{"     { return addToken(sym.lbrace, "lbrace", yytext()); }
"}"     { return addToken(sym.rbrace, "rbrace", yytext()); }
"<"     { return addToken(sym.lt, "lt", yytext()); }
">"     { return addToken(sym.gt, "gt", yytext()); }
"%"     { return addToken(sym.mod, "mod", yytext()); }
"!"     { return addToken(sym.not, "not", yytext()); }
"."     { return addToken(sym.dot, "dot", yytext()); }
","     { return addToken(sym.comma, "comma", yytext()); }

// Key Words
"print"     { return addToken(sym.imprimir, "imprimir", yytext()); }
"true"      { return addToken(sym.kwTrue,    "kwTrue", yytext()); }
"false"     { return addToken(sym.kwFalse,   "kwFalse", yytext()); }
"if"        { return addToken(sym.kwIf,      "kwIf", yytext()); }
"else"      { return addToken(sym.kwElse,    "kwElse", yytext()); }
"for"       { return addToken(sym.kwFor,     "kwFor", yytext()); }
"break"     { return addToken(sym.kwBreak,   "kwBreak", yytext()); }
"var"       { return addToken(sym.kwVar,     "kwVar", yytext()); }
"continue"  { return addToken(sym.kwContinue, "kwContinue", yytext()); }
"fmt"       { return addToken(sym.kwFmt,     "kwFmt", yytext()); }
"func"      { return addToken(sym.kwFunc,    "kwFunc", yytext()); }
"main"      { return addToken(sym.kwMain,    "kwMain", yytext()); }
"Println"   { return addToken(sym.kwPrintln, "kwPrintln", yytext()); }
"int"       { return addToken(sym.kwInt,     "kwInt", yytext()); }
"float64"   { return addToken(sym.kwFloat64, "kwFloat64", yytext()); }
"string"    { return addToken(sym.kwString,  "kwString", yytext()); }
"bool"      { return addToken(sym.kwBool,    "kwBool", yytext()); }
"rune"      { return addToken(sym.kwRune,    "kwRune", yytext()); }
"strconv"   { return addToken(sym.kwStrconv, "kwStrconv", yytext()); }
"reflect"   { return addToken(sym.kwReflect, "kwReflect", yytext()); }
"Atoi"      { return addToken(sym.kwAtoi,    "kwAtoi", yytext()); }
"ParseFloat" { return addToken(sym.kwParseFloat, "kwParseFloat", yytext()); }
"TypeOf"    { return addToken(sym.kwTypeOf,  "kwTypeOf", yytext()); }

// ID - String
{identifier}                { return addToken(sym.id, "id", yytext()); }
\"{str_lex}\"               { return addToken(sym.string, "string", yytext()); }

// Ignorar
{whitespace}    {/* pass */}
{Comment}       {/* pass */}

// Error
.   { errors.add(new GoliteError("Lexico", "Caracter no reconocido: " + yytext(), yyline, yycolumn)); }
