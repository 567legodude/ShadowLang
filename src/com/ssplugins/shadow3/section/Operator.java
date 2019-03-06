package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;

public class Operator extends ShadowSection {
    
    public Operator(TokenLine line, Token[] tokens) {
        super(line, tokens);
    }
    
    public String getSymbol() {
        return getPrimaryToken().getRaw();
    }
    
}
