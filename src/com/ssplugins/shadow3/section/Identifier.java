package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;

public class Identifier extends ShadowSection {
    
    public Identifier(TokenLine line, Token[] token) {
        super(line, token);
    }
    
    public String getName() {
        return getPrimaryToken().getRaw();
    }
    
}
