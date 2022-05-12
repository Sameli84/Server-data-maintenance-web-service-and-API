package com.example.servermaintenance.interpreter.parser;

import com.example.servermaintenance.interpreter.ast.*;
import com.example.servermaintenance.interpreter.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class ParserTest {
    @Test
    void testOperatorPrecedence() {
        record EE(String input, String expectedOutput) {
        }
        var ee = new ArrayList<EE>() {{
            add(new EE("id * 7 ^ 2 + (\"moi\" + (id -1))", "((id * (7 ^ 2)) + (\"moi\" + (id - 1)))"));
            add(new EE("\"moi\" + 1 + 2", "((\"moi\" + 1) + 2)"));
            add(new EE("\"moi\" + (1 + 2)", "(\"moi\" + (1 + 2))"));
            add(new EE("--a", "(-(-a))"));
            add(new EE("a + b + c", "((a + b) + c)"));
            add(new EE("a + b * c", "(a + (b * c))"));
            add(new EE("a / b * c", "((a / b) * c)"));
            add(new EE("a / b * c ^ d", "((a / b) * (c ^ d))"));
        }};

        for (var e : ee) {
            Parser parser = new Parser(new Lexer(e.input()));
            var program = parser.parseProgram();
            assertEquals(0, parser.getErrors().size(), "Parser errors");
            assertEquals(e.expectedOutput(), program.toString());
        }
    }

    @Test
    void testInfixExpressions() {
        record Infix(String input, Object left, String operator, Object right) {
        }
        var infixes = new ArrayList<Infix>() {{
            add(new Infix("5 + 5", 5, "+", 5));
            add(new Infix("5 - 5", 5, "-", 5));
            add(new Infix("5 * 5", 5, "*", 5));
            add(new Infix("5 / 5", 5, "/", 5));
            add(new Infix("5 ^ 5", 5, "^", 5));
            add(new Infix("5 % 5", 5, "%", 5));
            add(new Infix("\"moi\" + \"kka\"", "moi", "+", "kka"));
            add(new Infix("\"moi\" + 6", "moi", "+", 6));
            add(new Infix("6 + \"moi\"", 6, "+", "moi"));
            add(new Infix("a + b", new Identifier(null, "a"), "+", new Identifier(null, "b")));
            add(new Infix("a - b", new Identifier(null, "a"), "-", new Identifier(null, "b")));
            add(new Infix("a * b", new Identifier(null, "a"), "*", new Identifier(null, "b")));
            add(new Infix("a / b", new Identifier(null, "a"), "/", new Identifier(null, "b")));
            add(new Infix("a ^ b", new Identifier(null, "a"), "^", new Identifier(null, "b")));
            add(new Infix("a % b", new Identifier(null, "a"), "%", new Identifier(null, "b")));
        }};

        for (var infix : infixes) {
            Parser parser = new Parser(new Lexer(infix.input()));
            var program = parser.parseProgram();
            assertEquals(0, parser.getErrors().size(), "Parser errors");

            assertEquals(1, program.getStatements().size(), "Wrong amount of statements");

            var stmt = program.getStatements().get(0);
            assertInstanceOf(ExpressionStatement.class, stmt);

            var exp = ((ExpressionStatement) stmt).getExpression();
            assertInstanceOf(InfixExpression.class, exp);

            var inf = (InfixExpression) exp;

            assertEquals(infix.operator(), inf.getOperator());

            testLiteralExpression(infix.left(), inf.getLeft());
            testLiteralExpression(infix.right(), inf.getRight());
        }
    }

    @Test
    void testPrefixExpressions() {
        record Prefix(String input, String operator, Object right) {
        }
        var prefixes = new ArrayList<Prefix>() {{
            add(new Prefix("-5", "-", 5));
            add(new Prefix("-x", "-", new Identifier(null, "x")));
        }};

        for (var prefix : prefixes) {
            Parser parser = new Parser(new Lexer(prefix.input()));
            var program = parser.parseProgram();
            assertEquals(0, parser.getErrors().size(), "Parser errors");

            assertEquals(1, program.getStatements().size(), "Wrong amount of statements");

            var stmt = program.getStatements().get(0);
            assertInstanceOf(ExpressionStatement.class, stmt);

            var exp = ((ExpressionStatement) stmt).getExpression();
            assertInstanceOf(PrefixExpression.class, exp);

            var inf = (PrefixExpression) exp;

            assertEquals(prefix.operator(), inf.getOperator());

            testLiteralExpression(prefix.right(), inf.getRight());
        }
    }

    void testLiteralExpression(Object o, Expression expression) {
        switch (o) {
            case Integer i -> testIntegerLiteral(i, expression);
            case String s -> testStringLiteral(s, expression);
            case Identifier i -> testIdentifier(i, expression);
            case null, default -> fail("no type branch for " + o);
        }
    }

    void testIntegerLiteral(Integer expected, Expression expression) {
        assertEquals(expected, ((IntegerLiteral) expression).getValue());
    }

    void testStringLiteral(String expected, Expression expression) {
        assertEquals(expected, ((StringLiteral) expression).getValue());
    }

    void testIdentifier(Identifier expected, Expression expression) {
        assertEquals(expected.getValue(), ((Identifier) expression).getValue());
    }
}