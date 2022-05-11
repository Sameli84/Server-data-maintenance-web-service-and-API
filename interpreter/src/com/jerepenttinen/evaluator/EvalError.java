package com.jerepenttinen.evaluator;

public record EvalError(String message) implements IEvalObject {
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
