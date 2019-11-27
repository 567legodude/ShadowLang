package com.ssplugins.shadow4.def.op;

import com.ssplugins.shadow4.util.Primitive;

import java.util.function.BiFunction;

public interface OperatorAction<L, R, O> extends BiFunction<L, R, O> {
    
    default O execute(Object left, Object right, Class<?> targetLeft, Class<?> targetRight) {
        return runUnsafe(Primitive.as(left, targetLeft), Primitive.as(right, targetRight));
    }
    
    @SuppressWarnings("unchecked")
    default O runUnsafe(Object left, Object right) {
        return apply((L) left, (R) right);
    }
    
}
