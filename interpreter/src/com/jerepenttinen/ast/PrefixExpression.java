package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public class PrefixExpression implements IExpression {
    private final Token token;
    private final String operator;
    private IExpression right;

    public PrefixExpression(Token token, String operator) {
        this.token = token;
        this.operator = operator;
    }

    @Override
    public void expressionNode() {}

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

    public IExpression getRight() {
        return right;
    }

    public void setRight(IExpression right) {
        this.right = right;
    }
}
