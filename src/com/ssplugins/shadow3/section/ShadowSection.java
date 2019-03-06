package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;

public abstract class ShadowSection {
    
    private TokenLine line;
    private Token[] tokens;
    
    public ShadowSection(TokenLine line, Token[] tokens) {
        this.line = line;
        this.tokens = tokens;
    }
    
    public Token getPrimaryToken() {
        return tokens[0];
    }
    
    public TokenLine getLine() {
        return line;
    }
    
    public Token[] getTokens() {
        return tokens;
    }
    
}
