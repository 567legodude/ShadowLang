package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.OperatorType;
import com.ssplugins.shadow3.section.Operator.OpOrder;

public class NumberOperatorType {
    
    private String token;
    private OpOrder order;
    
    private OperatorType.OperatorAction<Integer, Integer, Integer> intOp;
    private OperatorType.OperatorAction<Double, Double, Double> doubleOp;
    private OperatorType.OperatorAction<Float, Float, Float> floatOp;
    private OperatorType.OperatorAction<Long, Long, Long> longOp;
    
    public NumberOperatorType(String token) {
        this(token, OperatorType.assumeOrder(token));
    }
    
    public NumberOperatorType(String token, OpOrder order) {
        this.token = token;
        this.order = order;
    }
    
    public NumberOperatorType(String token, OperatorType.OperatorAction<Integer, Integer, Integer> intOp, OperatorType.OperatorAction<Double, Double, Double> doubleOp, OperatorType.OperatorAction<Float, Float, Float> floatOp, OperatorType.OperatorAction<Long, Long, Long> longOp) {
        this(token, OperatorType.assumeOrder(token), intOp, doubleOp, floatOp, longOp);
    }
    
    public NumberOperatorType(String token, OpOrder order, OperatorType.OperatorAction<Integer, Integer, Integer> intOp, OperatorType.OperatorAction<Double, Double, Double> doubleOp, OperatorType.OperatorAction<Float, Float, Float> floatOp, OperatorType.OperatorAction<Long, Long, Long> longOp) {
        this.token = token;
        this.order = order;
        this.intOp = intOp;
        this.doubleOp = doubleOp;
        this.floatOp = floatOp;
        this.longOp = longOp;
    }
    
    public void addTo(ShadowContext context) {
        if (intOp != null) {
            OperatorType<Integer, Integer, Integer> intType = new OperatorType<>(token, order, Integer.class, Integer.class, Integer.class, intOp);
            intType.setMatcher(OperatorType.OperatorMatcher.forInt());
            context.addOperator(intType);
        }
        if (doubleOp != null) {
            OperatorType<Double, Double, Double> doubleType = new OperatorType<>(token, order, Double.class, Double.class, Double.class, doubleOp);
            doubleType.setMatcher(OperatorType.OperatorMatcher.forDouble());
            context.addOperator(doubleType);
        }
        if (floatOp != null) {
            OperatorType<Float, Float, Float> floatType = new OperatorType<>(token, order, Float.class, Float.class, Float.class, floatOp);
            floatType.setMatcher(OperatorType.OperatorMatcher.forFloat());
            context.addOperator(floatType);
        }
        if (longOp != null) {
            OperatorType<Long, Long, Long> longType = new OperatorType<>(token, order, Long.class, Long.class, Long.class, longOp);
            longType.setMatcher(OperatorType.OperatorMatcher.forLong());
            context.addOperator(longType);
        }
    }
    
}
