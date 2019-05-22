package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

public class ShadowString extends ShadowSection {
    
    private String value;
    
    public ShadowString(TokenReader reader) {
        super(reader.getLine());
        setToken(reader.expect(TokenType.STRING));
        value = getPrimaryToken().getRaw();
        value = value.substring(1, value.length() - 1);
    }
    
    @Override
    public Object toObject(Scope scope) {
        return value;
    }
    
    public String getValue() {
        return value;
    }
    
}
