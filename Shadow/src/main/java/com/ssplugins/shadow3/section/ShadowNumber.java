package com.ssplugins.shadow3.section;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.util.CompileScope;

public class ShadowNumber extends ShadowSection {
    
    private Number value;
    
    public ShadowNumber(TokenReader reader) {
        super(reader.getLine());
        setToken(reader.expect(TokenType.NUMBER));
        String raw = getPrimaryToken().getRaw();
        if (raw.indexOf('.') > -1) {
            if (raw.endsWith("f")) {
                raw = raw.substring(0, raw.length() - 1);
                value = Float.parseFloat(raw);
            }
            else value = Double.parseDouble(raw);
        }
        else if (raw.endsWith("L")) {
            raw = raw.substring(0, raw.length() - 1);
            value = Long.parseLong(raw);
        }
        else value = Integer.parseInt(raw);
    }
    
    @Override
    public Object toObject(Scope scope) {
        return value;
    }
    
    @Override
    public Class<?> getReturnType(CompileScope scope) {
        return value.getClass();
    }
    
    @Override
    public Code getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        return Code.format("$L", value);
    }
    
    public Number getValue() {
        return value;
    }
    
}
