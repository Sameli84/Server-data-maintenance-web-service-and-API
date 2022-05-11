package com.jerepenttinen.parser;

import com.jerepenttinen.ast.*;
import com.jerepenttinen.token.Token;
import com.jerepenttinen.token.TokenType;
import com.jerepenttinen.lexer.Lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Parser {
    private final Lexer lexer;
    private final List<String> errors;

    private Token curToken;
    private Token peekToken;

    private final HashMap<TokenType, Function<IExpression, IExpression>> infixParseFn = new HashMap<>() {{
        put(TokenType.PLUS, parseInfixExpression());
        put(TokenType.MINUS, parseInfixExpression());
        put(TokenType.SLASH, parseInfixExpression());
        put(TokenType.ASTERISK, parseInfixExpression());
        put(TokenType.PERCENT, parseInfixExpression());
        put(TokenType.CARET, parseInfixExpression());
    }};

    private final HashMap<TokenType, Supplier<IExpression>> prefixParseFn = new HashMap<>() {{
        put(TokenType.IDENT, parseIdentifier());
        put(TokenType.INT, parseIntegerLiteral());
        put(TokenType.MINUS, parsePrefixExpression());
        put(TokenType.LPAREN, parseGroupedExpression());
        put(TokenType.STRING, parseStringExpression());
        put(TokenType.ID, parseIdentifier());
    }};

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.errors = new ArrayList<>();

        nextToken();
        nextToken();
    }

    public Program parseProgram() {
        var program = new Program();

        while (!curTokenIs(TokenType.EOL)) {
            program.addStatement(parseExpressionStatement());
            nextToken();
        }
        return program;
    }

    private void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    boolean curTokenIs(TokenType t) {
        return curToken.type() == t;
    }

    boolean peekTokenIs(TokenType t) {
        return peekToken.type() == t;
    }

    boolean expectPeek(TokenType t) {
        if (peekTokenIs(t)) {
            nextToken();
            return true;
        } else {
            peekError(t);
            return false;
        }
    }

    void peekError(TokenType t) {
        errors.add(String.format("expected next token to be %s, got %s instead", t, peekToken.type()));
    }

    void noPrefixParseFnError(TokenType t) {
        errors.add(String.format("no prefix parse function for %s found", t));
    }

    private ExpressionStatement parseExpressionStatement() {
        return new ExpressionStatement(curToken, parseExpression(pre.LOWEST));
    }

    private IExpression parseExpression(pre precedence) {
        var prefix = prefixParseFn.get(curToken.type());
        if (prefix == null) {
            noPrefixParseFnError(curToken.type());
            return null;
        }
        var leftExpression = prefix.get();

        // precedence < peekPrecedence()
        while (precedence.compareTo(peekPrecedence()) < 0) {
            var infix = infixParseFn.get(peekToken.type());
            if (infix == null) {
                return leftExpression;
            }

            nextToken();
            leftExpression = infix.apply(leftExpression);
        }

        return leftExpression;
    }

    private pre peekPrecedence() {
        return precedences.getOrDefault(peekToken.type(), pre.LOWEST);
    }

    private pre curPrecedence() {
        return precedences.getOrDefault(curToken.type(), pre.LOWEST);
    }

    private Function<IExpression, IExpression> parseInfixExpression() {
        return (IExpression left) -> {
            var expression = new InfixExpression(curToken, left, curToken.literal());
            var precedence = curPrecedence();
            nextToken();
            expression.setRight(parseExpression(precedence));
            return expression;
        };
    }

    private Supplier<IExpression> parseIdentifier() {
        return () -> new Identifier(curToken, curToken.literal());
    }

    private Supplier<IExpression> parseIntegerLiteral() {
        return () -> {
            try {
                int value = Integer.parseInt(curToken.literal());
                return new IntegerLiteral(curToken, value);
            } catch (NumberFormatException e) {
                errors.add(String.format("could not parse %s as integer", curToken.literal()));
                return null;
            }
        };
    }

    private Supplier<IExpression> parsePrefixExpression() {
        return () -> {
            var expression = new PrefixExpression(curToken, curToken.literal());
            nextToken();
            expression.setRight(parseExpression(pre.PREFIX));
            return expression;
        };
    }

    private Supplier<IExpression> parseGroupedExpression() {
        return () -> {
            nextToken();
            var expression = parseExpression(pre.LOWEST);
            if (!expectPeek(TokenType.RPAREN)) {
                return null;
            }
            return expression;
        };
    }

    private Supplier<IExpression> parseStringExpression() {
        return () -> new StringLiteral(curToken, curToken.literal());
    }

    private static final HashMap<TokenType, pre> precedences = new HashMap<>() {{
        put(TokenType.PLUS, pre.SUM);
        put(TokenType.MINUS, pre.SUM);
        put(TokenType.ASTERISK, pre.PRODUCT);
        put(TokenType.SLASH, pre.PRODUCT);
        put(TokenType.PERCENT, pre.PRODUCT);
        put(TokenType.CARET, pre.POWER);
    }};

    public List<String> getErrors() {
        return errors;
    }
}

enum pre {
    LOWEST,
    SUM,
    PRODUCT,
    POWER,
    PREFIX
}