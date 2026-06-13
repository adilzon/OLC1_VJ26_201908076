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
%8bit  // recibir caracteres en formato UTF-8
// %debug // Habilitar modo debug para ver el proceso de tokenización
%ignorecase // ignorar mayusculas y minusculas
//%unicode

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
letter = [a-zA-Z]
whitespace = [\ \r\t\f\n]+
escape_char = \\ [\"\\nrt]
normal_char = [^\"\\\n\r]
str_lex = ({normal_char} | {escape_char})*

%%

// Numbers
{digit}+\.{digit}+  { return addToken(sym.decimal, "decimal", yytext()); }
{digit}+            { return addToken(sym.integer, "integer", yytext()); }

// Symbols
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
":="    { return addToken(sym.assign, "assign", yytext()); }
"<"     { return addToken(sym.lt, "lt", yytext()); }

// Key Words
"print"     { return addToken(sym.imprimir, "imprimir", yytext()); }
"true"      { return addToken(sym.kwTrue,    "kwTrue", yytext()); }
"false"     { return addToken(sym.kwFalse,   "kwFalse", yytext()); }
"if"        { return addToken(sym.kwIf,      "kwIf", yytext()); }
"else"      { return addToken(sym.kwElse,    "kwElse", yytext()); }
"for"       { return addToken(sym.kwFor,     "kwFor", yytext()); }
"break"     { return addToken(sym.kwBreak,   "kwBreak", yytext()); }

// ID - String
{letter}({letter}|{digit})* { return addToken(sym.id, "id", yytext()); }
\"{str_lex}\"               { return addToken(sym.string, "string", yytext()); }

// Ignorar
{whitespace}    {/* pass */}

// Error
.   { errors.add(new GoliteError("Lexico", "Caracter no reconocido: " + yytext(), yyline, yycolumn)); }
