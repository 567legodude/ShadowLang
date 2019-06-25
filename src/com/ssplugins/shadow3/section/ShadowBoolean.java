package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

public class ShadowBoolean extends ShadowSection {
    
    private boolean value;
    
    public ShadowBoolean(TokenReader reader) {
        super(reader.getLine());
        Token token = reader.expect(TokenType.BOOLEAN);
        setToken(token);
        value = Boolean.valueOf(token.getRaw());
    }
    
    @Override
    public Object toObject(Scope scope) {
        return value;
    }
    
}
