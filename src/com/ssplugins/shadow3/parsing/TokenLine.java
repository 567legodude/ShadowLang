package com.ssplugins.shadow3.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TokenLine {
    
    private String raw;
    private int line;
    private List<Token> tokens;
    
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
    
    public Optional<Token> lastCodeToken() {
        for (int i = tokens.size() - 1; i > 0; --i) {
            Token token = tokens.get(i);
            if (token.getType() != TokenType.NONE && token.getType() != TokenType.COMMENT) return Optional.of(token);
        }
        return Optional.empty();
    }
    
    public boolean endsWith(int type) {
        return endsWith(type, null);
    }
    
    public boolean endsWith(int type, String raw) {
        return lastCodeToken()
                .filter(token -> token.getType() == type)
                .filter(token -> raw == null || token.getRaw().equals(raw))
                .isPresent();
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
    
}
