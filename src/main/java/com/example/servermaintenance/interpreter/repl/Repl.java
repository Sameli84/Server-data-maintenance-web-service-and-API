package com.example.servermaintenance.interpreter.repl;


import com.example.servermaintenance.interpreter.evaluator.Environment;
import com.example.servermaintenance.interpreter.evaluator.Evaluator;
import com.example.servermaintenance.interpreter.lexer.Lexer;
import com.example.servermaintenance.interpreter.parser.Parser;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Repl {
    public static void Start(InputStream in, PrintStream out) {
        var scanner = new Scanner(in);

        while (true) {
            var scanned = scanner.nextLine();
            if (scanned.isEmpty()) {
                return;
            }

            var lexer = new Lexer(scanned);
            var parser = new Parser(lexer);

            var program = parser.parseProgram();

            if (parser.getErrors().size() != 0) {
                printParserErrors(out, parser.getErrors());
                continue;
            }

            var env = new Environment();
            for (int i = 1; i <= 5; i++) {
                env.putInteger("id", i);
                var evaluated = Evaluator.eval(program, env);
                if (evaluated != null) {
                    out.println(evaluated);
                }
            }
        }
    }

    private static void printParserErrors(PrintStream out, List<String> errors) {
        out.println("ERRORS:");
        for (var error : errors) {
            out.printf("\t%s\n", error);
        }
    }
}
