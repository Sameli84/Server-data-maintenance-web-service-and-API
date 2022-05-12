package com.example.servermaintenance.interpreter.ast;

import com.example.servermaintenance.interpreter.token.Token;

public record IntegerLiteral(Token token, int value) implements Expression {

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
