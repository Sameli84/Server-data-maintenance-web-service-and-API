package com.example.servermaintenance.interpreter.ast;

import com.example.servermaintenance.interpreter.token.Token;

public record Identifier(Token token, String value) implements Expression {

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
