package olc1.golite.lexer;

import olc1.golite.reports.Token;
import olc1.golite.reports.GoliteError;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple lexical analyzer for the GoLite language.
 *
 * The lexer now accepts a {@link Reader} (e.g., BufferedReader) to match the existing test harness.
 * It immediately tokenizes the entire input, populating {@code tokens} and {@code errors} lists.
 */
public class Lexer {
    private final String input;
    private int pos; // current index in input
    private int line; // 1‑based line number
    private int column; // 1‑based column number (position of next char)
    // collections for reports
    public final java.util.List<Token> tokens = new java.util.ArrayList<>();
    public final java.util.List<olc1.golite.reports.GoliteError> errors = new java.util.ArrayList<>();

    private static final Map<String, String> KEYWORDS = new HashMap<>();
    static {
        String[] kws = {"var", "func", "if", "else", "elif", "for", "while", "break", "continue",
                "return", "true", "false", "nil", "case", "switch", "default"};
        for (String kw : kws) {
            KEYWORDS.put(kw, "KEYWORD");
        }
    }

    /**
     * Constructs a lexer from a {@link BufferedReader}. The entire source is read into a string and tokenized.
     */
    public Lexer(BufferedReader reader) {
        // read all lines into a single string
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            errors.add(new GoliteError("IO", e.getMessage(), 0, 0));
        }
        this.input = sb.toString();
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        // Pre‑tokenize the whole source for easy access in tests
        Token tok;
        do {
            tok = nextToken();
            tokens.add(tok);
        } while (!tok.getType().equals("EOF"));
    }


    // Expose current token for external use (optional)
    private Token currentToken;
    
    /**
     * Returns the next token from the input, or an EOF token when the end is reached.
     */
    public Token nextToken() {
        skipWhitespaceAndComments();
        if (isAtEnd()) {
            return new Token("EOF", "", line, column);
        }
        char c = peek();
        // Identifier or keyword
        if (isAlpha(c) || c == '_') {
            return identifierOrKeyword();
        }
        // Numeric literal
        if (Character.isDigit(c)) {
            return numberLiteral();
        }
        // String literal
        if (c == '"') {
            return stringLiteral();
        }
        // Rune literal (single quoted character)
        if (c == '\'') {
            return runeLiteral();
        }
        // Operators and delimiters
        return operatorOrDelimiter();
    }

    // ---------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------
    private void skipWhitespaceAndComments() {
        while (true) {
            while (!isAtEnd() && Character.isWhitespace(peek())) {
                advance();
            }
            if (match("//")) { // single‑line comment
                while (!isAtEnd() && peek() != '\n') {
                    advance();
                }
                continue;
            } else if (match("/*")) { // multi‑line comment
                while (!isAtEnd() && !match("*/")) {
                    advance();
                }
                continue;
            }
            break;
        }
    }

    private Token identifierOrKeyword() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && (isAlphaNumeric(peek()) || peek() == '_')) {
            sb.append(advance());
        }
        String lexeme = sb.toString();
        String type = KEYWORDS.getOrDefault(lexeme, "IDENTIFIER");
        return new Token(type, lexeme, startLine, startCol);
    }

    private Token numberLiteral() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }
        // fractional part
        if (!isAtEnd() && peek() == '.' && (pos + 1 < input.length()) && Character.isDigit(input.charAt(pos + 1))) {
            sb.append(advance()); // consume '.'
            while (!isAtEnd() && Character.isDigit(peek())) {
                sb.append(advance());
            }
            return new Token("FLOAT_LITERAL", sb.toString(), startLine, startCol);
        }
        return new Token("INT_LITERAL", sb.toString(), startLine, startCol);
    }

    private Token stringLiteral() {
        int startLine = line;
        int startCol = column;
        advance(); // consume opening '"'
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\\') { // escape
                advance();
                if (isAtEnd()) break;
                char esc = advance();
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc); break;
                }
            } else {
                sb.append(advance());
            }
        }
        if (!isAtEnd()) advance(); // consume closing quote
        return new Token("STRING_LITERAL", sb.toString(), startLine, startCol);
    }

    private Token runeLiteral() {
        int startLine = line;
        int startCol = column;
        advance(); // opening '\''
        char value = '\0';
        if (!isAtEnd()) {
            if (peek() == '\\') {
                advance();
                if (!isAtEnd()) {
                    char esc = advance();
                    switch (esc) {
                        case '\'': value = '\''; break;
                        case '"': value = '"'; break;
                        case '\\': value = '\\'; break;
                        case 'n': value = '\n'; break;
                        case 'r': value = '\r'; break;
                        case 't': value = '\t'; break;
                        default: value = esc; break;
                    }
                }
            } else {
                value = advance();
            }
        }
        // expect closing '\''
        if (!isAtEnd() && peek() == '\'') {
            advance();
        }
        return new Token("RUNE_LITERAL", String.valueOf(value), startLine, startCol);
    }

    private Token operatorOrDelimiter() {
        int startLine = line;
        int startCol = column;
        // multi‑character operators first
        String[][] ops = {
                {"==", "EQUAL"}, {"!=", "NOT_EQUAL"}, {"<=", "LESS_EQUAL"}, {">=", "GREATER_EQUAL"},
                {"&&", "AND"}, {"||", "OR"}, {"+=", "PLUS_ASSIGN"}, {"-=", "MINUS_ASSIGN"}
        };
        for (String[] op : ops) {
            if (match(op[0])) {
                return new Token("OPERATOR", op[0], startLine, startCol);
            }
        }
        char c = advance();
        switch (c) {
            case '+': return new Token("OPERATOR", "+", startLine, startCol);
            case '-': return new Token("OPERATOR", "-", startLine, startCol);
            case '*': return new Token("OPERATOR", "*", startLine, startCol);
            case '/': return new Token("OPERATOR", "/", startLine, startCol);
            case '%': return new Token("OPERATOR", "%", startLine, startCol);
            case '!': return new Token("OPERATOR", "!", startLine, startCol);
            case '<': return new Token("OPERATOR", "<", startLine, startCol);
            case '>': return new Token("OPERATOR", ">", startLine, startCol);
            case '=': return new Token("OPERATOR", "=", startLine, startCol);
            case ';': return new Token("DELIMITER", ";", startLine, startCol);
            case ',': return new Token("DELIMITER", ",", startLine, startCol);
            case '.': return new Token("DELIMITER", ".", startLine, startCol);
            case '(': return new Token("DELIMITER", "(", startLine, startCol);
            case ')': return new Token("DELIMITER", ")", startLine, startCol);
            case '{': return new Token("DELIMITER", "{", startLine, startCol);
            case '}': return new Token("DELIMITER", "}", startLine, startCol);
            case '[': return new Token("DELIMITER", "[", startLine, startCol);
            case ']': return new Token("DELIMITER", "]", startLine, startCol);
            default:
                // Unknown character – report lexical error
                errors.add(new GoliteError("LEXICAL", "Invalid character: " + c, startLine, startCol));
                return new Token("UNKNOWN", String.valueOf(c), startLine, startCol);
        }
    }

    // ----------------------- low‑level utilities -----------------------
    private boolean isAtEnd() {
        return pos >= input.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : input.charAt(pos);
    }

    private char advance() {
        char c = peek();
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private boolean match(String s) {
        if (input.regionMatches(pos, s, 0, s.length())) {
            for (int i = 0; i < s.length(); i++) advance();
            return true;
        }
        return false;
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || Character.isDigit(c);
    }
}
