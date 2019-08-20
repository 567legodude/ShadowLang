package com.ssplugins.shadow3.section;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.util.CompileScope;

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
    
    @Override
    public Class<?> getReturnType(CompileScope scope) {
        return String.class;
    }
    
    @Override
    public Code getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        return Code.format("$S", value);
    }
    
    public String getValue() {
        return value;
    }
    
}
