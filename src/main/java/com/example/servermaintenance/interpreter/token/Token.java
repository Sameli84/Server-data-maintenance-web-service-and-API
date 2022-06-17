package com.example.servermaintenance.interpreter.token;

public record Token(TokenType type, String literal) {
    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public TokenType type() {
        return type;
    }

    @Override
    public String literal() {
        return literal;
    }
}
