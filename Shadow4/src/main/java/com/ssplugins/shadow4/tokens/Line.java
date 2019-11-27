package com.ssplugins.shadow4.tokens;

import java.util.ArrayList;
import java.util.List;

public class Line {
    
    private String line;
    private int lineNumber;
    private List<Token> tokens;
    
    private boolean isBlock;
    private int blockToken;
    
    public Line(String line, int lineNumber) {
        this.line = line;
        this.lineNumber = lineNumber;
        tokens = new ArrayList<>();
    }
    
    public Line(String line, int lineNumber, List<Token> tokens) {
        this.line = line;
        this.lineNumber = lineNumber;
        this.tokens = tokens;
    }
    
    public int tokenCount() {
        return tokens.size();
    }
    
    public String getLine() {
        return line;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
    
    public boolean isBlock() {
        return isBlock;
    }
    
    public void setBlock(boolean block, int blockToken) {
        isBlock = block;
        this.blockToken = blockToken;
    }
    
    public int getBlockToken() {
        return blockToken;
    }
    
}
