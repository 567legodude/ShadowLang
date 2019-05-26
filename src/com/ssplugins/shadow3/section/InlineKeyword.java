package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

public class InlineKeyword extends ShadowSection {
    
    private Keyword keyword;
    
    public InlineKeyword(ShadowEntity parent, TokenReader reader, ShadowParser parser) {
        super(reader.getLine());
        reader.setLimit(reader.size() - 1);
        reader.expect(TokenType.GROUP_OPEN, "[");
        keyword = new Keyword(parent, reader, parser.getContext());
        reader.reset();
        reader.expect(TokenType.GROUP_CLOSE, "]");
        keyword.setInline(true);
    }
    
    @Override
    public Object toObject(Scope scope) {
        return keyword.execute(scope.getStepper(), scope, null);
    }
    
}
