package com.ssplugins.shadow3.section;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.util.CompileScope;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyword extends ShadowSection {
    
    private Keyword keyword;
    
    public InlineKeyword(ShadowEntity parent, TokenReader reader, ShadowParser parser) {
        super(reader.getLine());
        
        int oldLimit = reader.getLimit();
        int end = (oldLimit == -1 ? reader.size() : oldLimit);
    
        int groupEnd = ShadowParser.findGroupEnd(reader.getTokens(), reader.getIndex(), end);
        if (groupEnd == ShadowParser.ERR_NO_CLOSING) {
            throw new ShadowParseError(getLine(), reader.peekNext().getIndex(), "No closing bracket found.");
        }
        else if (groupEnd == ShadowParser.ERR_TOO_MANY_CLOSING) {
            throw new ShadowParseError(getLine(), reader.peekNext().getIndex(), "Too many closing tokens after bracket.");
        }
    
        TokenReader keywordReader = reader.subReader(reader.getIndex() + 1, groupEnd);
    
        List<Token> tokens = new ArrayList<>();
        reader.setLimit(groupEnd);
        tokens.add(reader.expect(TokenType.GROUP_OPEN, "["));
        while (reader.hasNext()) {
            tokens.add(reader.next());
        }
        reader.setLimit(oldLimit);
        tokens.add(reader.expect(TokenType.GROUP_CLOSE, "]"));
        setTokens(tokens.toArray(new Token[0]));
        
        parent.addCompleteCallback(() -> {
            keyword = new Keyword(parent, keywordReader, parser.getContext());
            keyword.setInline(true);
        });
    }
    
    @Override
    public Object toObject(Scope scope) {
        return keyword.execute(scope.getStepper(), scope, null);
    }
    
    @Override
    public Class<?> getReturnType(CompileScope scope) {
        Class<?> returnType = keyword.getReturnType();
        if (returnType == null) keyword.findReturnType(scope);
        return keyword.getReturnType();
    }
    
    @Override
    public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        return keyword.getGeneration(context, type, method);
    }
    
    public Keyword getKeyword() {
        return keyword;
    }
    
}
