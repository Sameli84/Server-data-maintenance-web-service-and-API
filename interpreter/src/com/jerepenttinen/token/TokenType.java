package com.jerepenttinen.token;

import java.util.HashMap;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOL("EOL"), // end of line

    // identifiers and literals
    IDENT("IDENT"),
    INT("INT"),
    STRING("STRING"),
    UNTERMINATED_STRING("UNTERMINATED_STRING"),

    // operators
    PLUS("+"),
    MINUS("-"),
    ASTERISK("*"),
    SLASH("/"),
    PERCENT("%"),
    CARET("^"),

    LPAREN("("),
    RPAREN(")");


    private final String s;

    TokenType(String string) {
        this.s = string;
    }

//    private static final HashMap<String, TokenType> keywords = new HashMap<>() {{
//    }};

    public static TokenType lookupIdent(String ident) {
//        return keywords.getOrDefault(ident, IDENT);
        return IDENT;
    }

    @Override
    public String toString() {
        return this.s;
    }
}
