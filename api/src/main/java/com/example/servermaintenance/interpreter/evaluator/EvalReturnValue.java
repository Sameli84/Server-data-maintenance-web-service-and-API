package com.example.servermaintenance.interpreter.evaluator;

public record EvalReturnValue(EvalObject value) implements EvalObject {
    @Override
    public EvalObjectType getType() {
        return EvalObjectType.RETURN_VALUE;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public EvalObject getValue() {
        return value;
    }
}
