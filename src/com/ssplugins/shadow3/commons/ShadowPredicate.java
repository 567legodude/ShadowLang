package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.util.Schema;

import java.util.function.Predicate;

public class ShadowPredicate {
    
    private TokenLine line;
    private int index;
    private Schema<Parameters> schema;
    
    public ShadowPredicate(TokenLine line, int index) {
        this.line = line;
        this.index = index;
        schema = new Schema<>();
    }
    
    public static Predicate<Parameters> match(int size, Class<?>... types) {
        return p -> {
            if (p.size() != size) return false;
            for (int i = 0; i < types.length; ++i) {
                if (!types[i].isInstance(p.getParams().get(i))) return false;
            }
            return true;
        };
    }
    
    public static <T> Predicate<Parameters> as(Class<T> type, Predicate<T> predicate) {
        return p -> predicate.test(type.cast(p.getParams().get(0)));
    }
    
    public ShadowPredicate validate(Predicate<Parameters> predicate) {
        schema.require(p -> {
            if (!predicate.test(p)) {
                throw new ShadowExecutionError(line, index, "Wrong input given for keyword.");
            }
            return true;
        });
        return this;
    }
    
    public ShadowPredicate test(Predicate<Parameters> predicate) {
        schema.require(predicate);
        return this;
    }
    
    public Predicate<Parameters> get() {
        return schema;
    }
    
}
