package com.ssplugins.shadow3.parsing;

public class Token {
    
    private int type = TokenType.NONE;
    private String raw;
    private int line, index;
    
    public Token(String raw, int line, int index) {
        this.raw = raw;
        this.line = line;
        this.index = index;
    }
    
    public Token(int type, String raw, int line, int index) {
        this.type = type;
        this.raw = raw;
        this.line = line;
        this.index = index;
    }
    
    public void append(Token other) {
        raw += other.getRaw();
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
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
