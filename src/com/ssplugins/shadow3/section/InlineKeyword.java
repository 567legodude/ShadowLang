package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyword extends ShadowSection {
    
    private Keyword keyword;
    
    public InlineKeyword(ShadowEntity parent, TokenReader reader, ShadowParser parser) {
        super(reader.getLine());
        
        int oldLimit = reader.getLimit();
        int end = (oldLimit == -1 ? reader.size() : oldLimit);
    
        int groupEnd = ShadowParser.findGroupEnd(reader.getLine().getTokens(), reader.getIndex(), end);
        if (groupEnd == ShadowParser.ERR_NO_CLOSING) {
            throw new ShadowParseError(getLine(), reader.peekNext().getIndex(), "No closing bracket found.");
        }
        else if (groupEnd == ShadowParser.ERR_TOO_MANY_CLOSING) {
            throw new ShadowParseError(getLine(), reader.peekNext().getIndex(), "Too many closing tokens after bracket.");
        }
    
        List<Token> tokens = new ArrayList<>();
    
        reader.setLimit(groupEnd);
        tokens.add(reader.expect(TokenType.GROUP_OPEN, "["));
        keyword = new Keyword(parent, reader, parser.getContext());
        tokens.addAll(keyword.getLine().getTokens());
        reader.setLimit(oldLimit);
        tokens.add(reader.expect(TokenType.GROUP_CLOSE, "]"));
        keyword.setInline(true);
    
        setTokens(tokens.toArray(new Token[0]));
    }
    
    @Override
    public Object toObject(Scope scope) {
        return keyword.execute(scope.getStepper(), scope, null);
    }
    
}
