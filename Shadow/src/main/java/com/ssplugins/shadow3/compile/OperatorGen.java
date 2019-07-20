package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface OperatorGen<L, R> {
    
    String generate(String leftGen, String rightGen, Class<L> left, Class<R> right, TypeSpec.Builder type, MethodSpec.Builder method);
    
    static <L, R> OperatorGen<L, R> between(String s) {
        return (leftGen, rightGen, left, right, type, method) -> {
            if (leftGen == null) return s + rightGen;
            return leftGen + " " + s + " " + rightGen;
        };
    }
    
}
