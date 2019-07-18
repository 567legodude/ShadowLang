package com.ssplugins.shadow3.compile;

import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;

public class TypeChecker {
    
    private CompileScope scope;
    private ShadowSection section;
    
    private Class<?> type;
    private boolean valid = true;
    
    private TypeChecker(CompileScope scope, ShadowSection section) {
        this.scope = scope;
        this.section = section;
    }
    
    public static TypeChecker check(CompileScope scope, ShadowSection section) {
        return new TypeChecker(scope, section);
    }
    
    public static void require(CompileScope scope, ShadowSection section, Class<?> type) {
        TypeChecker.check(scope, section).is(type).orError();
    }
    
    // Will be false for different number types.
    public TypeChecker is(Class<?> type) {
        this.type = type;
        valid = type.isAssignableFrom(section.getReturnType(scope));
        return this;
    }
    
    public Class<?> orError() {
        if (valid) return type;
        throw new ShadowParseError(section.getLine(), section.index(), "Expected type \"" + type.getSimpleName() + "\" here.");
    }
    
}
