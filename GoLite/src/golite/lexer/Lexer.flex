package golite.lexer;

import java_cup.runtime.Symbol;
import golite.parser.sym;

%%

%class Lexer
%unicode
%cup
%line
%column
%public

%{

private Symbol symbol(int type){
    return new Symbol(type, yyline + 1, yycolumn + 1);
}

private Symbol symbol(int type, Object value){
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
}

%}

DIGITO      = [0-9]
LETRA       = [a-zA-Z_]
ENTERO      = {DIGITO}+
DECIMAL     = {DIGITO}+"."{DIGITO}+
ID          = {LETRA}({LETRA}|{DIGITO})*
CADENA      = \"([^\"\\]|\\.)*\"
RUNE        = \'([^\'\\]|\\.)\'
COMENTARIO1 = "//".*
COMENTARIO2 = "/*"([^*]|[\r\n]|(\*+([^*/]|[\r\n])))*"*"+"/"
WHITESPACE  = [ \t\r\n]+

%%

/* ===== PALABRAS RESERVADAS ===== */

"var"          { return symbol(sym.VAR, yytext()); }
"if"           { return symbol(sym.IF, yytext()); }
"else"         { return symbol(sym.ELSE, yytext()); }
"for"          { return symbol(sym.FOR, yytext()); }
"break"        { return symbol(sym.BREAK, yytext()); }
"continue"     { return symbol(sym.CONTINUE, yytext()); }

"true"         { return symbol(sym.TRUE, true); }
"false"        { return symbol(sym.FALSE, false); }
"nil"          { return symbol(sym.NIL, null); }

"int"          { return symbol(sym.TINT, yytext()); }
"float64"      { return symbol(sym.TFLOAT, yytext()); }
"string"       { return symbol(sym.TSTRING, yytext()); }
"bool"         { return symbol(sym.TBOOL, yytext()); }
"rune"         { return symbol(sym.TRUNE, yytext()); }

/* ===== FUNCIONES EMBEBIDAS ===== */

"fmt.Println"              { return symbol(sym.PRINTLN, yytext()); }
"strconv.Atoi"             { return symbol(sym.ATOI, yytext()); }
"strconv.ParseFloat"       { return symbol(sym.PARSEFLOAT, yytext()); }
"reflect.TypeOf"           { return symbol(sym.TYPEOF, yytext()); }

/* ===== OPERADORES ===== */

"=="           { return symbol(sym.IGUALIGUAL); }
"!="           { return symbol(sym.DIFERENTE); }
"<="           { return symbol(sym.MENORIGUAL); }
">="           { return symbol(sym.MAYORIGUAL); }
"&&"           { return symbol(sym.AND); }
"||"           { return symbol(sym.OR); }

"+="           { return symbol(sym.MASIGUAL); }
"-="           { return symbol(sym.MENOSIGUAL); }
":="           { return symbol(sym.DOSPUNTOIGUAL); }
"++"           { return symbol(sym.MASMAS); }
"--"           { return symbol(sym.MENOSMENOS); }

"+"            { return symbol(sym.MAS); }
"-"            { return symbol(sym.MENOS); }
"*"            { return symbol(sym.POR); }
"/"            { return symbol(sym.DIV); }
"%"            { return symbol(sym.MOD); }

"="            { return symbol(sym.IGUAL); }

"<"            { return symbol(sym.MENOR); }
">"            { return symbol(sym.MAYOR); }
"!"            { return symbol(sym.NOT); }

/* ===== DELIMITADORES ===== */

"("            { return symbol(sym.PARIZQ); }
")"            { return symbol(sym.PARDER); }
"{"            { return symbol(sym.LLAVEIZQ); }
"}"            { return symbol(sym.LLAVEDER); }

","            { return symbol(sym.COMA); }
";"            { return symbol(sym.PUNTOCOMA); }
"."            { return symbol(sym.PUNTO); }

/* ===== LITERALES ===== */

{DECIMAL}      { return symbol(sym.LITFLOAT, Double.parseDouble(yytext())); }
{ENTERO}       { return symbol(sym.LITINT, Integer.parseInt(yytext())); }
{CADENA}       { return symbol(sym.LITSTRING, yytext()); }
{RUNE}         { return symbol(sym.LITRUNE, yytext()); }
{ID}           { return symbol(sym.ID, yytext()); }

/* ===== IGNORAR ===== */

{COMENTARIO1}  { }
{COMENTARIO2}  { }
{WHITESPACE}   { }

/* ===== ERROR ===== */

[^] {
    return symbol(sym.ERROR, yytext());
}