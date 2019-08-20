package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface JavaComponent {
    
    Code getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method);
    
}
