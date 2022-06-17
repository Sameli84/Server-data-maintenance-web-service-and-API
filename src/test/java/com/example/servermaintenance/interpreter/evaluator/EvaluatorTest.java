package com.example.servermaintenance.interpreter.evaluator;

import com.example.servermaintenance.interpreter.lexer.Lexer;
import com.example.servermaintenance.interpreter.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {
    @Test
    void testEval() {
        record EE(String input, String expectedOutput) {
        }

        var ee = new ArrayList<EE>() {{
            add(new EE("1 + 2 + 3", "6"));
            add(new EE("1 + \"moi\" + 3", "1moi3"));
        }};

        for (var e : ee) {
            Parser parser = new Parser(new Lexer(e.input()));
            var program = parser.parseProgram();
            assertEquals(0, parser.getErrors().size(), "Parser errors");

            var evaluated = Evaluator.eval(program, new Environment());
            assert evaluated != null;
            assertEquals(e.expectedOutput(), evaluated.toString());
        }
    }
}