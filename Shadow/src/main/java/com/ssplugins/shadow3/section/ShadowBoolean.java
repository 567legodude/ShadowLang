package com.ssplugins.shadow3.section;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.util.CompileScope;

public class ShadowBoolean extends ShadowSection {
    
    private boolean value;
    
    public ShadowBoolean(TokenReader reader) {
        super(reader.getLine());
        Token token = reader.expect(TokenType.BOOLEAN);
        setToken(token);
        value = Boolean.parseBoolean(token.getRaw());
    }
    
    @Override
    public Object toObject(Scope scope) {
        return value;
    }
    
    @Override
    public Class<?> getReturnType(CompileScope scope) {
        return Boolean.class;
    }
    
    @Override
    public Code getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        return Code.format("$L", value);
    }
    
}
