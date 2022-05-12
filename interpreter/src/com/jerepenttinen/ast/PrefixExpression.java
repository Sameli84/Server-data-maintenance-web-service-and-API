package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public class PrefixExpression implements Expression {
    private final Token token;
    private final String operator;
    private Expression right;

    public PrefixExpression(Token token, String operator) {
        this.token = token;
        this.operator = operator;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return String.format("(%s%s)", operator, right);
    }

    public Token getToken() {
        return token;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }
}
