package com.example.servermaintenance.interpreter.evaluator;


import com.example.servermaintenance.interpreter.ast.*;

import java.util.Objects;

public class Evaluator {
    public static EvalObject eval(AstNode node, Environment env) {
        switch (node) {
            // statement
            case Program program:
                return evalProgram(program, env);

            case ExpressionStatement expressionStatement:
                return eval(expressionStatement.getExpression(), env);

            // expressions
            case IntegerLiteral integerLiteral:
                return new EvalInteger(integerLiteral.getValue());

            case StringLiteral stringLiteral:
                return new EvalString(stringLiteral.getValue());

            case PrefixExpression prefixExpression: {
                var right = eval(prefixExpression.getRight(), env);
                if (isError(right)) {
                    return right;
                }
                return evalPrefixExpression(prefixExpression.getOperator(), right);
            }

            case InfixExpression infixExpression:
                var left = eval(infixExpression.getLeft(), env);
                if (left == null || isError(left)) {
                    return left;
                }

                var right = eval(infixExpression.getRight(), env);
                if (right == null || isError(right)) {
                    return right;
                }

                return evalInfixExpression(infixExpression.getOperator(), left, right);

            case Identifier identifier:
                return evalIdentifier(identifier, env);

            default:
                return null;
        }
    }

    private static EvalObject evalProgram(Program program, Environment env) {
        EvalObject result = null;

        for (var statement : program.getStatements()) {
            result = eval(statement, env);
            switch (result) {
                case EvalReturnValue evalReturnValue:
                    return evalReturnValue.getValue();
                case EvalError evalError:
                    return evalError;
                case null:
                    break;
                default:
            }
        }
        return result;
    }

    private static EvalObject evalPrefixExpression(String operator, EvalObject right) {
        if (Objects.equals(operator, "-")) {
            if (right.getType() != EvalObjectType.INTEGER) {
                return newError("unknown operator: -%s", right.getType());
            }
            return new EvalInteger(-((EvalInteger) right).getValue());
        } else {
            return newError("unknown operator: %s%s", operator, right.getType());
        }
    }

    private static EvalObject evalInfixExpression(String operator, EvalObject left, EvalObject right) {
        if (left.getType() == EvalObjectType.INTEGER && right.getType() == EvalObjectType.INTEGER) {
            return evalIntegerInfixExpression(operator, left, right);

        } else if (left.getType() == EvalObjectType.STRING && right.getType() == EvalObjectType.STRING) {
            return evalStringInfixExpression(operator, left, right);

        } else if (left.getType() == EvalObjectType.STRING && right.getType() == EvalObjectType.INTEGER) {
            var rightStr = new EvalString(Integer.toString(((EvalInteger) right).getValue()));
            return evalStringInfixExpression(operator, left, rightStr);

        } else if (left.getType() == EvalObjectType.INTEGER && right.getType() == EvalObjectType.STRING) {
            var leftStr = new EvalString(Integer.toString(((EvalInteger) left).getValue()));
            return evalStringInfixExpression(operator, leftStr, right);

        } else {
            return newError("unknown operator: %s %s %s", left.getType(), operator, right.getType());
        }
    }

    private static EvalObject evalIdentifier(Identifier identifier, Environment env) {
        return env.getOrDefault(identifier.getValue(), newError("unexpected identifier: %s", identifier.getValue()));
    }

    private static EvalObject evalIntegerInfixExpression(String operator, EvalObject left, EvalObject right) {
        int leftVal = ((EvalInteger) left).getValue();
        int rightVal = ((EvalInteger) right).getValue();
        return switch (operator) {
            case "+" -> new EvalInteger(leftVal + rightVal);
            case "-" -> new EvalInteger(leftVal - rightVal);
            case "*" -> new EvalInteger(leftVal * rightVal);
            case "/" -> new EvalInteger(leftVal / rightVal);
            case "%" -> new EvalInteger(leftVal % rightVal);
            case "^" -> new EvalInteger((int) Math.pow(leftVal, rightVal));
            default -> newError("unknown operator: %s %s %s", left.getType(), operator, right.getType());
        };
    }

    private static EvalObject evalStringInfixExpression(String operator, EvalObject left, EvalObject right) {
        if (!Objects.equals(operator, "+")) {
            return newError("unknown operator: %s %s %s", left.getType(), operator, right.getType());
        }
        String leftVal = ((EvalString) left).getValue();
        String rightVal = ((EvalString) right).getValue();
        return new EvalString(leftVal + rightVal);
    }

    private static boolean isError(EvalObject object) {
        if (object != null) {
            return object.getType() == EvalObjectType.ERROR;
        }
        return false;
    }

    private static EvalError newError(String format, Object... args) {
        return new EvalError(String.format(format, args));
    }
}
