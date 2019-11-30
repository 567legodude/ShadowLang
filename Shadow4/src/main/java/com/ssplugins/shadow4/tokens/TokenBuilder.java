package com.ssplugins.shadow4.tokens;

import com.ssplugins.shadow4.exception.SourceCodeException;

import java.util.ArrayList;
import java.util.List;

public class TokenBuilder {
    
    private String source;
    private int charIndex;
    private TokenFilter tokenFilter;
    
    private List<Token> tokens = new ArrayList<>();
    private StringBuilder currentToken = new StringBuilder();
    private int tokenIndex = -1;
    private TokenType currentType = TokenType.NONE;
    
    public TokenBuilder(String source, TokenFilter tokenFilter) {
        this.source = source;
        this.tokenFilter = tokenFilter;
    }
    
    public void finishLine() {
        charIndex = source.length();
    }
    
    public void addToToken() {
        currentToken.append(source.charAt(charIndex - 1));
        if (tokenIndex == -1) {
            tokenIndex = charIndex - 1;
        }
    }
    
    public void finishToken(int line) {
        if (currentToken.length() == 0) return;
        if (currentType == TokenType.NONE) {
            throw new SourceCodeException(source, line, tokenIndex, "Token type was not set for \"" + currentToken.toString() + "\"");
        }
        if (tokenFilter != null) {
            tokenFilter.accept(currentToken.toString(), this);
        }
        tokens.add(new Token(currentType, currentToken.toString(), line, tokenIndex));
        currentToken.setLength(0);
        tokenIndex = -1;
        currentType = TokenType.NONE;
    }
    
    public void finishOtherType(TokenType type, int lineNumber) {
        if (currentType != type) {
            finishToken(lineNumber);
        }
    }
    
    public boolean addEscaped() {
        if (!hasNextChar()) return false;
        this.nextChar();
        this.addToToken();
        return true;
    }
    
    public boolean addUntil(char end, char escape) {
        while (hasNextChar()) {
            char c = this.nextChar();
            if (c == escape) {
                if (!this.addEscaped()) return false;
            }
            else {
                this.addToToken();
            }
            if (c == end) return true;
        }
        return false;
    }
    
    public void hint(TokenType type) {
        if (currentType == TokenType.NONE) {
            currentType = type;
        }
    }
    
    public boolean isEmptyToken() {
        return currentToken.length() == 0;
    }
    
    public int charIndex() {
        return charIndex - 1;
    }
    
    public int tokenSize() {
        return currentToken.length();
    }
    
    public boolean hasNextChar() {
        return charIndex < source.length();
    }
    
    public char nextChar() {
        return source.charAt(charIndex++);
    }
    
    public void setSource(String source) {
        this.source = source;
        charIndex = 0;
        tokens = new ArrayList<>();
        currentToken.setLength(0);
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
    
    public String getCurrentToken() {
        return currentToken.toString();
    }
    
    public int getTokenIndex() {
        return tokenIndex;
    }
    
    public TokenType getCurrentType() {
        return currentType;
    }
    
    public void setCurrentType(TokenType currentType) {
        this.currentType = currentType;
    }
    
}
