package com.jerepenttinen.evaluator;

public record EvalInteger(int value) implements IEvalObject {
    @Override
    public EvalObjectType getType() {
        return EvalObjectType.INTEGER;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public int getValue() {
        return value;
    }
}
