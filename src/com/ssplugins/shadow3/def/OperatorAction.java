package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.section.Operator.OpOrder;

import java.util.function.BiFunction;

public class OperatorAction<L, R, O> {
    
    private String token;
    private OpOrder order;
    private Class<L> leftType;
    private Class<R> rightType;
    private Class<O> outputType;
    private BiFunction<L, R, O> action;
    
    public OperatorAction(String token, Class<L> leftType, Class<R> rightType, Class<O> outputType, BiFunction<L, R, O> action) {
        this.token = token;
        this.leftType = leftType;
        this.rightType = rightType;
        this.outputType = outputType;
        this.action = action;
        order = OpOrder.get(token).orElseThrow(ShadowException.arg("Cannot assume operator precedence of \"" + token + "\""));
    }
    
    public OperatorAction(String token, OpOrder order, Class<L> leftType, Class<R> rightType, Class<O> outputType, BiFunction<L, R, O> action) {
        this.token = token;
        this.order = order;
        this.leftType = leftType;
        this.rightType = rightType;
        this.outputType = outputType;
        this.action = action;
    }
    
    public boolean isPlaceholder() {
        return leftType == null && rightType == null;
    }
    
    public String getToken() {
        return token;
    }
    
    public OpOrder getOrder() {
        return order;
    }
    
    public Class<L> getLeftType() {
        return leftType;
    }
    
    public Class<R> getRightType() {
        return rightType;
    }
    
    public Class<O> getOutputType() {
        return outputType;
    }
    
    public BiFunction<L, R, O> getAction() {
        return action;
    }
    
}
