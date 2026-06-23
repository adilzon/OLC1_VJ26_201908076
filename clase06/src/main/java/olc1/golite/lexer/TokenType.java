package olc1.golite.lexer;

/**
 * TokenType enumerates all lexical token categories recognized by the Lexer.
 * The string value of each enum constant will be used as the token type
 * when constructing {@link olc1.golite.reports.Token} objects.
 */
public enum TokenType {
    // Literals
    INT_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    BOOL_LITERAL,
    // Identifiers & keywords
    IDENTIFIER,
    // Keywords (a subset; add more as needed)
    KW_FUNC,
    KW_VAR,
    KW_IF,
    KW_ELSE,
    KW_FOR,
    KW_WHILE,
    KW_RETURN,
    KW_BREAK,
    KW_CONTINUE,
    KW_TRUE,
    KW_FALSE,
    // Operators
    OP_PLUS,
    OP_MINUS,
    OP_MULT,
    OP_DIV,
    OP_MOD,
    OP_ASSIGN,
    OP_EQ,
    OP_NEQ,
    OP_LT,
    OP_GT,
    OP_LE,
    OP_GE,
    OP_NOT,
    OP_AND,
    OP_OR,
    OP_PLUS_ASSIGN,
    OP_MINUS_ASSIGN,
    // Delimiters & punctuation
    SEMICOLON,
    COMMA,
    DOT,
    COLON,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    // Comments (handled internally, not emitted as tokens)
    COMMENT,
    // End of file
    EOF
}
