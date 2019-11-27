package com.ssplugins.shadow4.def;

import com.ssplugins.shadow4.compile.OperatorCodeGen;
import com.ssplugins.shadow4.def.op.OperatorAction;

public class OperatorType<L, R, O> {
    
    private Class<L> leftType;
    private Class<R> rightType;
    private Class<O> outputType;
    private OperatorAction<L, R, O> action;
    private OperatorCodeGen<L, R> generator;
    
    public OperatorType(Class<L> leftType, Class<R> rightType, Class<O> outputType, OperatorAction<L, R, O> action, OperatorCodeGen<L, R> generator) {
        this.leftType = leftType;
        this.rightType = rightType;
        this.outputType = outputType;
        this.action = action;
        this.generator = generator;
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
    
    public OperatorAction<L, R, O> getAction() {
        return action;
    }
    
    public OperatorCodeGen<L, R> getGenerator() {
        return generator;
    }
    
}
