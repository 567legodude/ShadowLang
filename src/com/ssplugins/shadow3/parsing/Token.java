package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.util.Schema;

import java.util.function.Predicate;

public class Token {
    
    private TokenType type = TokenType.NONE;
    private String raw;
    private int line, index;
    
    public Token(String raw, int line, int index) {
        this.raw = raw;
        this.line = line;
        this.index = index;
    }
    
    public Token(TokenType type, String raw, int line, int index) {
        this.type = type;
        this.raw = raw;
        this.line = line;
        this.index = index;
    }
    
    public static Schema<Token> matcher() {
        return new Schema<>();
    }
    
    public static Predicate<Token> is(TokenType type) {
        return is(type, null);
    }
    
    public static Predicate<Token> is(TokenType type, String raw) {
        return token -> token.getType() == type && (raw == null || token.getRaw().equals(raw));
    }
    
    public void append(Token other) {
        raw += other.getRaw();
    }
    
    public int indexAfter() {
        return index + raw.length();
    }
    
    public TokenType getType() {
        return type;
    }
    
    public void setType(TokenType type) {
        this.type = type;
    }
    
    public String getRaw() {
        return raw;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getIndex() {
        return index;
    }
    
    @Override
    public String toString() {
        return raw;
    }
    
}
