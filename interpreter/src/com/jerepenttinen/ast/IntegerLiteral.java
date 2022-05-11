package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public record IntegerLiteral(Token token, int value) implements IExpression {
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

    public int getValue() {
        return value;
    }
}
