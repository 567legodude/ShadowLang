package com.ssplugins.shadow3.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenLine {
    
    private String raw;
    private int line;
    private List<Token> tokens;
    
    private boolean isBlock;
    private int blockEnd = -1;
    
    public TokenLine(String raw, int line) {
        this.raw = raw;
        this.line = line;
        tokens = new ArrayList<>();
    }
    
    public TokenLine(String raw, int line, List<Token> tokens) {
        this.raw = raw;
        this.line = line;
        this.tokens = tokens;
    }
    
    public static TokenLine empty(int line) {
        return new TokenLine("", line, Collections.emptyList());
    }
    
    public Token lastToken() {
        return tokens.get(tokens.size() - 1);
    }
    
    public boolean endsWith(TokenType type) {
        return endsWith(type, null);
    }
    
    public boolean endsWith(TokenType type, String raw) {
        Token last = lastToken();
        return last.getType() == type && (raw == null || last.getRaw().equals(raw));
    }
    
    public int size() {
        return tokens.size();
    }
    
    public String getRaw() {
        return raw;
    }
    
    public int getLineNumber() {
        return line;
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
    
    public boolean isBlock() {
        return isBlock;
    }
    
    public void setBlock(boolean block, int index) {
        isBlock = block;
        blockEnd = index;
    }
    
    public int getBlockEnd() {
        return blockEnd;
    }
    
}
