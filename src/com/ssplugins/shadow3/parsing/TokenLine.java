package com.ssplugins.shadow3.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    
    public String getRaw() {
        return raw;
    }
    
    public int getLine() {
        return line;
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
    
}
