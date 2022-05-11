package com.jerepenttinen.evaluator;

public record EvalString(String value) implements IEvalObject {
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
