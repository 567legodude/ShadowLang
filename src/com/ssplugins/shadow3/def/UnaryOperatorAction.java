package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.section.Operator.OpOrder;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UnaryOperatorAction<R, O> extends OperatorAction<Void, R, O> {
    
    public UnaryOperatorAction(String token, Class<R> rightType, Class<O> outputType, Function<R, O> action) {
        super(token, OpOrder.UNARY, Void.class, rightType, outputType, (v, r) -> action.apply(r));
    }
    
    public UnaryOperatorAction(String token, OpOrder order, Class<Void> leftType, Class<R> rightType, Class<O> outputType, BiFunction<Void, R, O> action) {
        super(token, order, leftType, rightType, outputType, action);
    }
    
}
