package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.entity.Keyword;

public interface KeywordGen {
    
    String generate(GenerateContext c, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method);
    
    static KeywordGen none() {
        return (c, keyword, type, method) -> null;
    }
    
}
