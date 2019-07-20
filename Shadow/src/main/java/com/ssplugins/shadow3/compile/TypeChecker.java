package com.ssplugins.shadow3.compile;

import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.NumberType;
import com.ssplugins.shadow3.util.Parameter;

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
    
    public static void require(Parameter parameter, Class<?> type, String error) {
        if (!NumberType.isAssignableFrom(type, parameter.getType())) {
            Identifier s = parameter.getIdentifier();
            throw new ShadowParseError(s.getLine(), s.index(), error);
        }
    }
    
    public static void require(ShadowSection section, boolean check, String error) {
        if (!check) {
            throw new ShadowParseError(section.getLine(), section.index(), error);
        }
    }
    
    public TypeChecker type(Class<?> type) {
        this.type = type;
        return this;
    }
    
    public TypeChecker isArray() {
        valid = section.getReturnType(scope).isArray();
        return this;
    }
    
    // Will be false for different number types.
    public TypeChecker is(Class<?> type) {
        this.type = type;
        valid = NumberType.isAssignableFrom(type, section.getReturnType(scope));
        return this;
    }
    
    public Class<?> orError() {
        if (valid) return type;
        throw new ShadowParseError(section.getLine(), section.index(), "Expected type \"" + type.getSimpleName() + "\" here.");
    }
    
    public boolean check() {
        return valid;
    }
    
}
