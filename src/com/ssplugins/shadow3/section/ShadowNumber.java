package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

public class ShadowNumber extends ShadowSection {
    
    private Number value;
    
    public ShadowNumber(TokenReader reader) {
        super(reader.getLine());
        setToken(reader.expect(TokenType.NUMBER));
        String raw = getPrimaryToken().getRaw();
        if (raw.indexOf('.') > -1) {
            if (raw.endsWith("f")) value = Float.parseFloat(raw);
            else value = Double.parseDouble(raw);
        }
        else if (raw.endsWith("L")) value = Long.parseLong(raw);
        else value = Integer.parseInt(raw);
    }
    
    @Override
    public Object toObject(Scope scope) {
        return value;
    }
    
    public Number getValue() {
        return value;
    }
    
}
