package com.ssplugins.shadow4.section;

import com.ssplugins.shadow4.tokens.Line;
import com.ssplugins.shadow4.tokens.Token;

import java.util.List;

public abstract class Section {
    
    private Line line;
    private List<Token> tokens;
    
    public Section(Line line) {
        this.line = line;
    }
    
    public void setTokens(int startIndex, int endIndex) {
        tokens = line.getTokens().subList(startIndex, endIndex);
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
    
}
