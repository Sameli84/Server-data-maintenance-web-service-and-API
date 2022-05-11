package com.jerepenttinen.token;

import java.util.HashMap;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOL("EOL"), // end of line

    // identifiers and literals
    IDENT("IDENT"),
    INT("INT"),
    STRING("STRING"),

    // operators
    PLUS("+"),
    MINUS("-"),
    ASTERISK("*"),
    SLASH("/"),
    PERCENT("%"),
    CARET("^"),

    LPAREN("("),
    RPAREN(")"),

    // keywords
    ID("ID");

    private final String s;

    TokenType(String string) {
        this.s = string;
    }

    private static final HashMap<String, TokenType> keywords = new HashMap<>() {{
        put("id", ID);
    }};

    public static TokenType lookupIdent(String ident) {
        return keywords.getOrDefault(ident, IDENT);
    }

    @Override
    public String toString() {
        return this.s;
    }
}
