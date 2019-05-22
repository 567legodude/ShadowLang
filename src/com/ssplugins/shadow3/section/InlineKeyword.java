package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;

public class InlineKeyword extends ShadowSection {
    
    public InlineKeyword(TokenReader reader) {
        super(reader.getLine());
        // TODO read keyword
    }
    
    @Override
    public Object toObject(Scope scope) {
        return null; // TODO forward to keyword.
    }
    
}
