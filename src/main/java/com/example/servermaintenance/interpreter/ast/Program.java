package com.example.servermaintenance.interpreter.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements AstNode {
    private final List<Statement> statements;

    public Program() {
        this.statements = new ArrayList<>();
    }

    @Override
    public String toString() {
        var out = new StringBuilder();
        for (var s : statements) {
            out.append(s);
        }
        return out.toString();
    }

    @Override
    public String tokenLiteral() {
        if (statements.size() > 0) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }
}
