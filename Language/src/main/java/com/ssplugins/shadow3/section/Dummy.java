package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;

public class Dummy extends ShadowSection {
    
    public Dummy(TokenReader reader) {
        super(reader.getLine());
        reader.consume();
    }
    
    @Override
    public Object toObject(Scope scope) {
        return null;
    }
    
}
