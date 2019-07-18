package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.section.ShadowSection;

public interface Transformer<T> {
    
    Object transform(T t);
    
    static <U> U getter(Class<U> expected, ShadowSection section, Scope scope) {
        Object o = section.toObject(scope);
        if (!expected.isInstance(o)) {
            throw new ShadowExecutionError(section.getLine(), section.getPrimaryToken().getIndex(), "Argument is not the correct type for this keyword.");
        }
        return expected.cast(o);
    }
    
}
