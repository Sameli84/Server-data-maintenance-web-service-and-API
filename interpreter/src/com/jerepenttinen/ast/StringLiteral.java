package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public record StringLiteral(Token token, String value) implements IExpression {
    @Override
    public void expressionNode() {
    }

    @Override
    public String tokenLiteral() {
        return String.format("\"%s\"", token.literal());
    }

    @Override
    public String toString() {
        return String.format("\"%s\"", token.literal());
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }
}
