package com.jerepenttinen.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements INode {
    private final List<IStatement> statements;

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

    public List<IStatement> getStatements() {
        return statements;
    }

    public void addStatement(IStatement statement) {
        statements.add(statement);
    }
}
