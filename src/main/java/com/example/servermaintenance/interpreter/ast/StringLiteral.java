package com.example.servermaintenance.interpreter.ast;

import com.example.servermaintenance.interpreter.token.Token;

public record StringLiteral(Token token, String value) implements Expression {

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
