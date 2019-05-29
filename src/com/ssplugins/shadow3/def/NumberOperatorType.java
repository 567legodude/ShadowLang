package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.OperatorType.OperatorAction;
import com.ssplugins.shadow3.def.OperatorType.OperatorMatcher;
import com.ssplugins.shadow3.section.Operator.OpOrder;

public class NumberOperatorType {
    
    private String token;
    private OpOrder order;
    
    private OperatorAction<Integer, Integer, Integer> intOp;
    private OperatorAction<Double, Double, Double> doubleOp;
    private OperatorAction<Float, Float, Float> floatOp;
    private OperatorAction<Long, Long, Long> longOp;
    
    public NumberOperatorType(String token) {
        this(token, OperatorType.assumeOrder(token));
    }
    
    public NumberOperatorType(String token, OpOrder order) {
        this.token = token;
        this.order = order;
    }
    
    public NumberOperatorType(String token, OperatorAction<Integer, Integer, Integer> intOp, OperatorAction<Double, Double, Double> doubleOp, OperatorAction<Float, Float, Float> floatOp, OperatorAction<Long, Long, Long> longOp) {
        this(token, OperatorType.assumeOrder(token), intOp, doubleOp, floatOp, longOp);
    }
    
    public NumberOperatorType(String token, OpOrder order, OperatorAction<Integer, Integer, Integer> intOp, OperatorAction<Double, Double, Double> doubleOp, OperatorAction<Float, Float, Float> floatOp, OperatorAction<Long, Long, Long> longOp) {
        this.token = token;
        this.order = order;
        this.intOp = intOp;
        this.doubleOp = doubleOp;
        this.floatOp = floatOp;
        this.longOp = longOp;
    }
    
    public void addTo(ShadowContext context) {
        if (intOp != null) {
            OperatorType<Integer, Integer, Integer> intType = new OperatorType<>(token, int.class, int.class, int.class, intOp);
            intType.setMatcher(OperatorMatcher.forInt());
            context.addOperator(intType);
        }
        if (doubleOp != null) {
            OperatorType<Double, Double, Double> doubleType = new OperatorType<>(token, double.class, double.class, double.class, doubleOp);
            doubleType.setMatcher(OperatorMatcher.forDouble());
            context.addOperator(doubleType);
        }
        if (floatOp != null) {
            OperatorType<Float, Float, Float> floatType = new OperatorType<>(token, float.class, float.class, float.class, floatOp);
            floatType.setMatcher(OperatorMatcher.forFloat());
            context.addOperator(floatType);
        }
        if (longOp != null) {
            OperatorType<Long, Long, Long> longType = new OperatorType<>(token, long.class, long.class, long.class, longOp);
            longType.setMatcher(OperatorMatcher.forLong());
            context.addOperator(longType);
        }
    }
    
}
