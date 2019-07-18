package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.util.CompileScope;

public interface Returnable {
    
    Class<?> getReturnType(Keyword keyword, CompileScope scope);
    
    static Class<?> empty() {
        return Void.class;
    }
    
    static Returnable none() {
        return (keyword, scope) -> Void.class;
    }
    
    static Returnable any() {
        return (keyword, scope) -> Object.class;
    }
    
    static Returnable of(Class<?> type) {
        return (keyword, scope) -> type;
    }

}
