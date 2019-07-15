package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.MethodSpec;

public interface OperatorGen<L, R> {
    
    void generate(MethodSpec.Builder method, Class<L> left, Class<R> right);
    
}
