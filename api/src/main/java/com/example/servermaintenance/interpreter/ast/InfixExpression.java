package com.example.servermaintenance.interpreter.ast;

import com.example.servermaintenance.interpreter.token.Token;

public class InfixExpression implements Expression {
    private final Token token;
    private final Expression left;
    private final String operator;
    private Expression right;

    public InfixExpression(Token token, Expression left, String operator) {
        this.token = token;
        this.left = left;
        this.operator = operator;
    }

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

    public Expression getLeft() {
        return left;
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
