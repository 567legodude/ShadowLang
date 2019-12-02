package com.ssplugins.shadow4.tokens;

public class Token {
    
    private TokenType type = TokenType.NONE;
    private String content;
    private int lineNumber;
    private int lineIndex;
    
    public Token(String content, int lineNumber, int lineIndex) {
        this.content = content;
        this.lineNumber = lineNumber;
        this.lineIndex = lineIndex;
    }
    
    public Token(TokenType type, String content, int lineNumber, int lineIndex) {
        this.content = content;
        this.type = type;
        this.lineNumber = lineNumber;
        this.lineIndex = lineIndex;
    }
    
    public boolean matches(TokenMatcher matcher) {
        return matcher.test(this);
    }
    
    public boolean matches(TokenType type, String content) {
        return this.type == type && (content == null || this.content.equals(content));
    }
    
    public String getContent() {
        return content;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getLineIndex() {
        return lineIndex;
    }
    
}
