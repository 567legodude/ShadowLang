package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Code {
    
    private static final Pattern VAR_MATCHER = Pattern.compile("\\$([A-Z])");
    
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
        return empty().append(format, args);
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
    
    // Similar to CodeBlock.Builder.add(String, Object...)
    // but will convert Code args to CodeBlocks
    public Code append(String s, Object... args) {
        Matcher m = VAR_MATCHER.matcher(s);
        int index = 0;
        int arg = 0;
        while (m.find()) {
            if (index < m.start()) {
                builder.add(s.substring(index, m.start()));
            }
            if (args[arg] instanceof Code) {
                this.append((Code) args[arg]);
            }
            else {
                builder.add(m.group(), args[arg]);
            }
            index = m.end();
            arg++;
        }
        if (index < s.length()) {
            builder.add(s.substring(index));
        }
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
