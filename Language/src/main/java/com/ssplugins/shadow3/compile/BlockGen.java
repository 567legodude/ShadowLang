package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.entity.Block;

public interface BlockGen {
    
    void generate(GenerateContext c, Block block, TypeSpec.Builder type, MethodSpec.Builder method);
    
}
