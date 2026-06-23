package olc1.golite.lexer;

import olc1.golite.reports.Token;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for the {@link Lexer} implementation.
 * These tests cover identifiers, literals, operators, comments and string escape handling.
 */
public class LexerTest {

    private List<Token> lex(String source) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(source));
        Lexer lexer = new Lexer(reader);
        return lexer.tokens; // tokens are pre‑populated in the constructor
    }

    @Test
    public void testIdentifierAndKeyword() throws Exception {
        List<Token> tokens = lex("var x = 10");
        assertEquals("KEYWORD", tokens.get(0).getType()); // var
        assertEquals("IDENTIFIER", tokens.get(1).getType()); // x
        assertEquals("OPERATOR", tokens.get(2).getType()); // =
        assertEquals("INT_LITERAL", tokens.get(3).getType()); // 10
        assertEquals("EOF", tokens.get(tokens.size() - 1).getType());
    }

    @Test
    public void testNumericLiterals() throws Exception {
        List<Token> tokens = lex("123 45.67");
        assertEquals("INT_LITERAL", tokens.get(0).getType());
        assertEquals("FLOAT_LITERAL", tokens.get(1).getType());
    }

    @Test
    public void testStringEscapes() throws Exception {
        List<Token> tokens = lex("\"hello\\nworld\\t\\\"\"");
        assertEquals("STRING_LITERAL", tokens.get(0).getType());
        assertEquals("hello\nworld\t\"", tokens.get(0).getLexeme());
    }

    @Test
    public void testCommentsAreIgnored() throws Exception {
        List<Token> tokens = lex("// line comment\nvar y = 5 /* block comment */");
        // Should see var, identifier, =, int literal, EOF
        assertEquals(5, tokens.size());
        assertEquals("KEYWORD", tokens.get(0).getType());
        assertEquals("IDENTIFIER", tokens.get(1).getType());
        assertEquals("OPERATOR", tokens.get(2).getType());
        assertEquals("INT_LITERAL", tokens.get(3).getType());
        assertEquals("EOF", tokens.get(4).getType());
    }
}
