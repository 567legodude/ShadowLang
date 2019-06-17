package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.OperatorType;
import com.ssplugins.shadow3.def.OperatorType.OperatorAction;
import com.ssplugins.shadow3.def.OperatorType.OperatorMatcher;
import com.ssplugins.shadow3.section.Operator.OpOrder;

public class NumberCompareOp {
    
    private String token;
    private OpOrder order;
    
    private OperatorAction<Integer, Integer, Boolean> intOp;
    private OperatorAction<Double, Double, Boolean> doubleOp;
    private OperatorAction<Float, Float, Boolean> floatOp;
    private OperatorAction<Long, Long, Boolean> longOp;
    
    public NumberCompareOp(String token) {
        this(token, OperatorType.assumeOrder(token));
    }
    
    public NumberCompareOp(String token, OpOrder order) {
        this.token = token;
        this.order = order;
    }
    
    public NumberCompareOp(String token, OperatorAction<Integer, Integer, Boolean> intOp, OperatorAction<Double, Double, Boolean> doubleOp, OperatorAction<Float, Float, Boolean> floatOp, OperatorAction<Long, Long, Boolean> longOp) {
        this(token, OperatorType.assumeOrder(token), intOp, doubleOp, floatOp, longOp);
    }
    
    public NumberCompareOp(String token, OpOrder order, OperatorAction<Integer, Integer, Boolean> intOp, OperatorAction<Double, Double, Boolean> doubleOp, OperatorAction<Float, Float, Boolean> floatOp, OperatorAction<Long, Long, Boolean> longOp) {
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
            intType.setMatcher(OperatorMatcher.forInt());
            context.addOperator(intType);
        }
        if (doubleOp != null) {
            OperatorType<Double, Double, Boolean> doubleType = new OperatorType<>(token, order, double.class, double.class, boolean.class, doubleOp);
            doubleType.setMatcher(OperatorMatcher.forDouble());
            context.addOperator(doubleType);
        }
        if (floatOp != null) {
            OperatorType<Float, Float, Boolean> floatType = new OperatorType<>(token, order, float.class, float.class, boolean.class, floatOp);
            floatType.setMatcher(OperatorMatcher.forFloat());
            context.addOperator(floatType);
        }
        if (longOp != null) {
            OperatorType<Long, Long, Boolean> longType = new OperatorType<>(token, order, long.class, long.class, boolean.class, longOp);
            longType.setMatcher(OperatorMatcher.forLong());
            context.addOperator(longType);
        }
    }
    
}
