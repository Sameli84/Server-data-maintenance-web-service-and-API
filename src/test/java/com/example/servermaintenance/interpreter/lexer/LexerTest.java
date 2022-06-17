package com.example.servermaintenance.interpreter.lexer;

import com.example.servermaintenance.interpreter.token.Token;
import com.example.servermaintenance.interpreter.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {
    record Pair(TokenType type, String literal) {
    }
    @Test
    void testNextToken() {
        var input = """
                1 * 3 / 5 ^ ident - 100 +"string" + (java % 5) * -(24^2) $ "unterminated
                """;
        var lexer = new Lexer(input);
        var lexedTokens = new ArrayList<Token>();
        Token token;
        do {
            token = lexer.nextToken();
            lexedTokens.add(token);
        } while (token.type() != TokenType.EOL);

        var expectedTokens = new ArrayList<Pair>() {{
            add(new Pair(TokenType.INT, "1"));
            add(new Pair(TokenType.ASTERISK, "*"));
            add(new Pair(TokenType.INT, "3"));
            add(new Pair(TokenType.SLASH, "/"));
            add(new Pair(TokenType.INT, "5"));
            add(new Pair(TokenType.CARET, "^"));
            add(new Pair(TokenType.IDENT, "ident"));
            add(new Pair(TokenType.MINUS, "-"));
            add(new Pair(TokenType.INT, "100"));
            add(new Pair(TokenType.PLUS, "+"));
            add(new Pair(TokenType.STRING, "string"));
            add(new Pair(TokenType.PLUS, "+"));
            add(new Pair(TokenType.LPAREN, "("));
            add(new Pair(TokenType.IDENT, "java"));
            add(new Pair(TokenType.PERCENT, "%"));
            add(new Pair(TokenType.INT, "5"));
            add(new Pair(TokenType.RPAREN, ")"));
            add(new Pair(TokenType.ASTERISK, "*"));
            add(new Pair(TokenType.MINUS, "-"));
            add(new Pair(TokenType.LPAREN, "("));
            add(new Pair(TokenType.INT, "24"));
            add(new Pair(TokenType.CARET, "^"));
            add(new Pair(TokenType.INT, "2"));
            add(new Pair(TokenType.RPAREN, ")"));
            add(new Pair(TokenType.ILLEGAL, "$"));
            add(new Pair(TokenType.UNTERMINATED_STRING, ""));
            add(new Pair(TokenType.EOL, ""));
        }};

        assertEquals(expectedTokens.size(), lexedTokens.size(), String.format("different sizes: wanted %d, got %d", expectedTokens.size(), lexedTokens.size()));

        for (int i = 0; i < lexedTokens.size(); i++) {
            assertEquals(expectedTokens.get(i).literal(), lexedTokens.get(i).literal());
            assertEquals(expectedTokens.get(i).type(), lexedTokens.get(i).type());
        }
    }
}