package com.example.servermaintenance.interpreter;

import com.example.servermaintenance.interpreter.ast.Program;
import com.example.servermaintenance.interpreter.evaluator.Environment;
import com.example.servermaintenance.interpreter.evaluator.EvalError;
import com.example.servermaintenance.interpreter.evaluator.Evaluator;
import com.example.servermaintenance.interpreter.lexer.Lexer;
import com.example.servermaintenance.interpreter.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
    private final Environment environment = new Environment();
    private final Program program;
    private final Parser parser;

    private List<String> errors;

    public Interpreter(final String statement) {
        this.parser = new Parser(new Lexer(statement));
        this.program = parser.parseProgram();
    }

    public Interpreter putInt(final String varName, int value) {
        this.environment.putInteger(varName, value);
        return this;
    }

    public Interpreter putLong(final String varName, long value) {
        this.environment.putInteger(varName, (int) value);
        return this;
    }

    public Interpreter declareString(final String varName, final String value) {
        this.environment.declareString(varName, value);
        return this;
    }

    public Interpreter putString(final String varName, final String value) {
        this.environment.putString(varName, value);
        return this;
    }

    public boolean hasErrors() {
        return this.errors != null && !this.errors.isEmpty();
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public String execute() {
        if (parser.getErrors().size() != 0) {
            this.errors = parser.getErrors();
            return parser.getErrors().get(0);
        }

        var result = Evaluator.eval(program, environment);
        if (result instanceof EvalError evalError) {
            if (this.errors == null) {
                this.errors = new ArrayList<>(1);
            }
            this.errors.add(evalError.message());
            return "";
        } else if (result == null) {
            return "";
        } else {
            return result.toString();
        }
    }
}
