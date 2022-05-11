package com.jerepenttinen;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Repl {
    public static void Start(InputStream stdin, PrintStream stdout) {
        var scanner = new Scanner(stdin);

        while (true) {
            var scanned = scanner.nextLine();
            if (scanned.isEmpty()) {
                return;
            }
            stdout.println("GOT: " + scanned);
            var l = new Lexer(scanned);
            var al = new ArrayList<Token>();
            Token tok;
            do {
                tok = l.nextToken();
                al.add(tok);
            } while (tok.type() != TokenType.EOL);

            for (var t : al) {
                stdout.println(t);
            }
        }
    }
}
