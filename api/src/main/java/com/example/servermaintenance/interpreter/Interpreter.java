package com.example.servermaintenance.interpreter;

import com.example.servermaintenance.interpreter.evaluator.Environment;
import com.example.servermaintenance.interpreter.evaluator.Evaluator;
import com.example.servermaintenance.interpreter.lexer.Lexer;
import com.example.servermaintenance.interpreter.parser.Parser;

public class Interpreter {
    public static String eval(String statement, long id) {
        var lexer = new Lexer(statement);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();

        if (parser.getErrors().size() != 0) {
            return parser.getErrors().get(0);
        }

        var env = new Environment();
        env.putInteger("id", (int)id);
        var evaluated = Evaluator.eval(program, env);
        return evaluated.toString();
    }
}
