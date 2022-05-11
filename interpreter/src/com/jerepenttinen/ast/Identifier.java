package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public record Identifier(Token token, String value) implements IExpression {
    @Override
    public void expressionNode() {
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return token.literal();
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }
}
