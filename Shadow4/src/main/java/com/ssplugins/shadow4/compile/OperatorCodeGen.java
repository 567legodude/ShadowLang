package com.ssplugins.shadow4.compile;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface OperatorCodeGen<L, R> {
    
    Code generate(Code leftGen, Code rightGen, Class<L> left, Class<R> right, TypeSpec.Builder type, MethodSpec.Builder method);
    
    static <L, R> OperatorCodeGen<L, R> between(String symbol) {
        return (leftGen, rightGen, left, right, type, method) -> {
            if (leftGen == null) return Code.plain(symbol).append(rightGen);
            return leftGen.append(" ").append(symbol).append(" ").append(rightGen);
        };
    }
    
}
