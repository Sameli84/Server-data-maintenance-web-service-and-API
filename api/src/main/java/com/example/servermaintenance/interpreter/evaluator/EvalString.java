package com.example.servermaintenance.interpreter.evaluator;

public record EvalString(String value) implements EvalObject {
    @Override
    public EvalObjectType getType() {
        return EvalObjectType.STRING;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
