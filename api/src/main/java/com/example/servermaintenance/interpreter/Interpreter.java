package com.example.servermaintenance.interpreter;

import com.example.servermaintenance.interpreter.ast.Program;
import com.example.servermaintenance.interpreter.evaluator.Environment;
import com.example.servermaintenance.interpreter.evaluator.Evaluator;
import com.example.servermaintenance.interpreter.lexer.Lexer;
import com.example.servermaintenance.interpreter.parser.Parser;

public class Interpreter {
    private final Environment environment = new Environment();
    private final Program program;
    private final Parser parser;

    public Interpreter(final String statement) {
        this.parser = new Parser(new Lexer(statement));
        this.program = parser.parseProgram();
    }

    public Interpreter declareInt(final String varName, int value) {
        this.environment.putInteger(varName, value);
        return this;
    }

    public Interpreter declareLong(final String varName, long value) {
        this.environment.putInteger(varName, (int) value);
        return this;
    }

    public Interpreter declareString(final String varName, final String value) {
        this.environment.putString(varName, value);
        return this;
    }

    public String execute() {
        if (parser.getErrors().size() != 0) {
            return parser.getErrors().get(0);
        }

        var result = Evaluator.eval(program, environment);
        if (result == null) {
            return "";
        } else {
            return result.toString();
        }
    }
}
