package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public record ExpressionStatement(Token token, IExpression expression) implements IStatement {
    @Override
    public void statementNode() {
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        if (expression != null) {
            return expression.toString();
        } else {
            return "";
        }
    }

    public Token getToken() {
        return token;
    }

    public IExpression getExpression() {
        return expression;
    }
}
