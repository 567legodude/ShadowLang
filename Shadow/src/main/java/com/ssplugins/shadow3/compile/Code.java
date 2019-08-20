package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

public class Code {
    
    private CodeBlock.Builder builder;
    
    public Code(CodeBlock.Builder builder) {
        this.builder = builder;
    }
    
    public static Code plain(String s) {
        return format(s);
    }
    
    public static Code wrap(CodeBlock block) {
        return new Code(block.toBuilder());
    }
    
    public static Code format(String format, Object... args) {
        return new Code(CodeBlock.builder().add(format, args));
    }
    
    public static Code empty() {
        return new Code(CodeBlock.builder());
    }
    
    public void addTo(MethodSpec.Builder method) {
        method.addStatement(builder.build());
    }
    
    public CodeBlock toCodeBlock() {
        return builder.build();
    }
    
    public Code append(Code other) {
        builder.add(other.toCodeBlock());
        return this;
    }
    
    public Code append(String s, Object... args) {
        builder.add(s, args);
        return this;
    }
    
    @Override
    public String toString() {
        return toCodeBlock().toString();
    }
    
    public CodeBlock.Builder getBuilder() {
        return builder;
    }
    
}
