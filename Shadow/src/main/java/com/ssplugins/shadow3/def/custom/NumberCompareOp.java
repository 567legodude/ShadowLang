package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.OperatorType;
import com.ssplugins.shadow3.section.Operator.OpOrder;

public class NumberCompareOp {
    
    private String token;
    private OpOrder order;
    
    private OperatorType.OperatorAction<Integer, Integer, Boolean> intOp;
    private OperatorType.OperatorAction<Double, Double, Boolean> doubleOp;
    private OperatorType.OperatorAction<Float, Float, Boolean> floatOp;
    private OperatorType.OperatorAction<Long, Long, Boolean> longOp;
    
    public NumberCompareOp(String token) {
        this(token, OperatorType.assumeOrder(token));
    }
    
    public NumberCompareOp(String token, OpOrder order) {
        this.token = token;
        this.order = order;
    }
    
    public NumberCompareOp(String token, OperatorType.OperatorAction<Integer, Integer, Boolean> intOp, OperatorType.OperatorAction<Double, Double, Boolean> doubleOp, OperatorType.OperatorAction<Float, Float, Boolean> floatOp, OperatorType.OperatorAction<Long, Long, Boolean> longOp) {
        this(token, OperatorType.assumeOrder(token), intOp, doubleOp, floatOp, longOp);
    }
    
    public NumberCompareOp(String token, OpOrder order, OperatorType.OperatorAction<Integer, Integer, Boolean> intOp, OperatorType.OperatorAction<Double, Double, Boolean> doubleOp, OperatorType.OperatorAction<Float, Float, Boolean> floatOp, OperatorType.OperatorAction<Long, Long, Boolean> longOp) {
        this.token = token;
        this.order = order;
        this.intOp = intOp;
        this.doubleOp = doubleOp;
        this.floatOp = floatOp;
        this.longOp = longOp;
    }
    
    public void addTo(ShadowContext context) {
        if (intOp != null) {
            OperatorType<Integer, Integer, Boolean> intType = new OperatorType<>(token, order, int.class, int.class, boolean.class, intOp);
            intType.setMatcher(OperatorType.OperatorMatcher.forInt());
            context.addOperator(intType);
        }
        if (doubleOp != null) {
            OperatorType<Double, Double, Boolean> doubleType = new OperatorType<>(token, order, double.class, double.class, boolean.class, doubleOp);
            doubleType.setMatcher(OperatorType.OperatorMatcher.forDouble());
            context.addOperator(doubleType);
        }
        if (floatOp != null) {
            OperatorType<Float, Float, Boolean> floatType = new OperatorType<>(token, order, float.class, float.class, boolean.class, floatOp);
            floatType.setMatcher(OperatorType.OperatorMatcher.forFloat());
            context.addOperator(floatType);
        }
        if (longOp != null) {
            OperatorType<Long, Long, Boolean> longType = new OperatorType<>(token, order, long.class, long.class, boolean.class, longOp);
            longType.setMatcher(OperatorType.OperatorMatcher.forLong());
            context.addOperator(longType);
        }
    }
    
}
