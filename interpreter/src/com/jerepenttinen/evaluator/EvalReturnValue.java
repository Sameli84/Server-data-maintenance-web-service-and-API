package com.jerepenttinen.evaluator;

public record EvalReturnValue(IEvalObject value) implements IEvalObject {
    @Override
    public EvalObjectType getType() {
        return EvalObjectType.RETURN_VALUE;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public IEvalObject getValue() {
        return value;
    }
}
