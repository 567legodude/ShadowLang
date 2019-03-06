package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;

public class ShadowString extends ShadowSection {
    
    private String value;
    
    public ShadowString(TokenLine line, Token[] tokens) {
        super(line, tokens);
        value = getPrimaryToken().getRaw();
        value = value.substring(0, value.length() - 1);
    }
    
    public String getValue() {
        return value;
    }
    
}
