package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;

public class ShadowNumber extends ShadowSection {
    
    private Number value;
    
    public ShadowNumber(TokenLine line, Token[] tokens) {
        super(line, tokens);
        String raw = getPrimaryToken().getRaw();
        if (raw.indexOf('.') > -1) {
            char last = raw.charAt(raw.length() - 1);
            if (last == 'f') value = Float.parseFloat(raw);
            else value = Double.parseDouble(raw);
        }
        else value = Integer.parseInt(raw);
    }
    
    public Number getValue() {
        return value;
    }
    
}
