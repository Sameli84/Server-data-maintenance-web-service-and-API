package com.jerepenttinen.ast;

import com.jerepenttinen.token.Token;

public record ExpressionStatement(Token token, Expression expression) implements Statement {
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

    public Expression getExpression() {
        return expression;
    }
}
