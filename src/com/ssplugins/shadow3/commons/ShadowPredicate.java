package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.util.Schema;

import java.util.function.Predicate;

public class ShadowPredicate extends InputModifier<Boolean> {
    
    private Schema<Parameters> schema;
    
    public ShadowPredicate(TokenLine line, int index) {
        super(line, index);
        schema = new Schema<>();
        setFunction(p -> schema.test(p));
    }
    
    public static Predicate<Parameters> matchAll(Class<?> type) {
        return p -> p.getParams().stream().allMatch(type::isInstance);
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
                throw new ShadowExecutionError(getLine(), getIndex(), "Wrong input given for keyword.");
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
