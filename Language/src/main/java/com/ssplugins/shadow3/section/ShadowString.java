package com.ssplugins.shadow3.section;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.GenerateContext;
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
    
    @Override
    public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        return CodeBlock.of("$S", value).toString();
    }
    
    public String getValue() {
        return value;
    }
    
}
