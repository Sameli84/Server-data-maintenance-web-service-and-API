package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public class InfixExpression implements IExpression {
    private final Token token;
    private final IExpression left;
    private final String operator;
    private IExpression right;

    public InfixExpression(Token token, IExpression left, String operator) {
        this.token = token;
        this.left = left;
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
        return String.format("(%s %s %s)", left, operator, right);
    }

    public Token getToken() {
        return token;
    }

    public IExpression getLeft() {
        return left;
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
