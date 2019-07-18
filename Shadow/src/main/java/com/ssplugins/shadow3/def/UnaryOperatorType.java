package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.section.Operator;

import java.util.function.Function;

public class UnaryOperatorType<R, O> extends OperatorType<Void, R, O> {
    
    public UnaryOperatorType(String token, Class<R> rightType, Class<O> outputType, Function<R, O> action) {
        super(token, Operator.OpOrder.UNARY, Void.class, rightType, outputType, (v, r) -> action.apply(r));
        setMatcher(OperatorMatcher.sameType());
    }
    
    public UnaryOperatorType(String token, Operator.OpOrder order, Class<Void> leftType, Class<R> rightType, Class<O> outputType, OperatorAction<Void, R, O> action) {
        super(token, order, leftType, rightType, outputType, action);
        setMatcher(OperatorMatcher.sameType());
    }
    
}
