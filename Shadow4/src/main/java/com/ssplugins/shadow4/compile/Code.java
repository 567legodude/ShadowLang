package com.ssplugins.shadow4.compile;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Code {
    
    private static final Pattern VARS = Pattern.compile("\\$[A-Z]");
    
    private CodeBlock.Builder builder;
    
    public Code(CodeBlock.Builder builder) {
        this.builder = builder;
    }
    
    public static Code wrap(CodeBlock block) {
        return new Code(block.toBuilder());
    }
    
    public static Code plain(String s) {
        return format(s);
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
    
    public Code append(Code other) {
        builder.add(other.toCodeBlock());
        return this;
    }
    
    public Code append(String format, Object... args) {
        if (args.length == 0) {
            builder.add(format);
            return this;
        }
        Matcher m = VARS.matcher(format);
        int index = 0;
        int arg = 0;
        while (m.find()) {
            if (index < m.start()) {
                builder.add(format.substring(index, m.start()));
            }
            if (args[arg] instanceof Code) {
                builder.add(((Code) args[arg]).toCodeBlock());
            }
            else {
                builder.add(m.group(), args[arg]);
            }
            index = m.end();
            arg++;
        }
        if (index < format.length()) {
            builder.add(format.substring(index));
        }
        return this;
    }
    
    public CodeBlock toCodeBlock() {
        return builder.build();
    }
    
}
