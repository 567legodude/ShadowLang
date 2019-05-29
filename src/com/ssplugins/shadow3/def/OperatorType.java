package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.OperatorMap;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.section.Operator.OpOrder;

import java.util.function.BiFunction;

public class OperatorType<L, R, O> {
    
    private String token;
    private OpOrder order;
    private boolean leftToRight = true;
    private Class<L> leftType;
    private Class<R> rightType;
    private Class<O> outputType;
    private OperatorMatcher matcher = OperatorMatcher.isAssignable();
    private OperatorAction<L, R, O> action;
    
    public OperatorType(String token, Class<L> leftType, Class<R> rightType, Class<O> outputType, OperatorAction<L, R, O> action) {
        this.token = token;
        this.leftType = leftType;
        this.rightType = rightType;
        this.outputType = outputType;
        this.action = action;
        order = assumeOrder(token);
    }
    
    public OperatorType(String token, OpOrder order, Class<L> leftType, Class<R> rightType, Class<O> outputType, OperatorAction<L, R, O> action) {
        this.token = token;
        this.order = order;
        this.leftType = leftType;
        this.rightType = rightType;
        this.outputType = outputType;
        this.action = action;
    }
    
    public static OpOrder assumeOrder(String token) {
        return OpOrder.get(token).orElseThrow(ShadowException.arg("Cannot assume operator precedence of \"" + token + "\""));
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
    
    public boolean isLeftToRight() {
        return leftToRight;
    }
    
    public void setLeftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
    }
    
    public Class<L> getLeftType() {
        return leftType;
    }
    
    public Class<?> getLeftWrap() {
        return OperatorMap.wrap(leftType);
    }
    
    public Class<R> getRightType() {
        return rightType;
    }
    
    public Class<?> getRightWrap() {
        return OperatorMap.wrap(rightType);
    }
    
    public Class<O> getOutputType() {
        return outputType;
    }
    
    public OperatorMatcher getMatcher() {
        return matcher;
    }
    
    public void setMatcher(OperatorMatcher matcher) {
        this.matcher = matcher;
    }
    
    public OperatorAction<L, R, O> getAction() {
        return action;
    }
    
    public interface OperatorAction<Left, Right, Output> extends BiFunction<Left, Right, Output> {
    
        default Output execute(Object l, Object r, Class<?> leftType, Class<?> rightType) {
            if (l instanceof Number) l = convert((Number) l, leftType);
            if (r instanceof Number) r = convert((Number) r, rightType);
            //noinspection unchecked
            return apply((Left) l, (Right) r);
        }
    
        static Object convert(Number number, Class<?> target) {
            if (target == Integer.class) return number.intValue();
            if (target == Double.class) return number.doubleValue();
            if (target == Float.class) return number.floatValue();
            if (target == Long.class) return number.longValue();
            return number;
        }
        
    }
    
    public interface OperatorMatcher {
    
        boolean matches(OperatorType<?, ?, ?> type, Object left, Object right);
    
        static OperatorMatcher isAssignable() {
            return (type, left, right) -> {
                if (left == null) return type.getRightWrap().isAssignableFrom(right.getClass());
                return type.getLeftWrap().isAssignableFrom(left.getClass()) && type.getRightWrap().isAssignableFrom(right.getClass());
            };
        }
    
        static OperatorMatcher sameType() {
            return (type, left, right) -> {
                if (left == null) return type.getRightWrap() == right.getClass();
                return type.getLeftWrap() == left.getClass() && type.getRightWrap() == right.getClass();
            };
        }
    
        static boolean notNumbers(Object left, Object right) {
            return !(left instanceof Number) || !(right instanceof Number);
        }
    
        static OperatorMatcher numbers() {
            return (type, left, right) -> {
                return left instanceof Number && right instanceof Number;
            };
        }
    
        static OperatorMatcher forDouble() {
            return (type, left, right) -> {
                if (notNumbers(left, right)) return false;
                return left instanceof Double || right instanceof Double;
            };
        }
    
        static OperatorMatcher forFloat() {
            return (type, left, right) -> {
                if (notNumbers(left, right)) return false;
                if (left instanceof Double || right instanceof Double) return false;
                return left instanceof Float || right instanceof Float;
            };
        }
    
        static OperatorMatcher forLong() {
            return (type, left, right) -> {
                if (notNumbers(left, right)) return false;
                if (left instanceof Double || right instanceof Double) return false;
                if (left instanceof Float || right instanceof Float) return false;
                return left instanceof Long || right instanceof Long;
            };
        }
    
        static OperatorMatcher forInt() {
            return (type, left, right) -> {
                if (notNumbers(left, right)) return false;
                return left instanceof Integer && right instanceof Integer;
            };
        }
    
    }
    
}
