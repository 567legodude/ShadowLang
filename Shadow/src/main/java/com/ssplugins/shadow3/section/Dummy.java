package com.ssplugins.shadow3.section;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.util.CompileScope;

public class Dummy extends ShadowSection {
    
    public Dummy(TokenReader reader) {
        super(reader.getLine());
        reader.consume();
    }
    
    @Override
    public Object toObject(Scope scope) {
        return null;
    }
    
    @Override
    public Class<?> getReturnType(CompileScope scope) {
        return null;
    }
    
    @Override
    public Code getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder builder) {
        return null;
    }
    
}
