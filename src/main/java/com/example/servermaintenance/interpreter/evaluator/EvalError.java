package com.example.servermaintenance.interpreter.evaluator;

public record EvalError(String message) implements EvalObject {
    @Override
    public EvalObjectType getType() {
        return EvalObjectType.ERROR;
    }

    @Override
    public String toString() {
        return "ERROR: " + message;
    }

    public String getMessage() {
        return message;
    }
}
