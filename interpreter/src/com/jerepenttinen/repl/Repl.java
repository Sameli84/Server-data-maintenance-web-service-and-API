package com.jerepenttinen.repl;

import com.jerepenttinen.evaluator.Evaluator;
import com.jerepenttinen.parser.Parser;
import com.jerepenttinen.token.Token;
import com.jerepenttinen.token.TokenType;
import com.jerepenttinen.lexer.Lexer;

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

            var evaluated = Evaluator.eval(program);
            if (evaluated != null) {
                out.println(evaluated);
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
