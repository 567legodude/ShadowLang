package com.ssplugins.shadow3.def.custom;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.entity.Keyword;

public interface CommandGen {
    
    String generate(String input, GenerateContext c, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method);
    
}
