package com.ssplugins.shadow3.api;

import com.ssplugins.shadow3.def.OperatorType;
import com.ssplugins.shadow3.def.OperatorType.OperatorMatcher;
import com.ssplugins.shadow3.section.Operator.OpOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperatorMap {
    
    private OpOrder order;
    private boolean leftToRight = true;
    private List<OperatorType<?, ?, ?>> types;
    private List<OperatorType<?, ?, ?>> search;
    
    public OperatorMap(OpOrder order, boolean leftToRight) {
        if (order != OpOrder.UNARY) this.order = order;
        this.leftToRight = leftToRight;
        types = new ArrayList<>();
        search = new ArrayList<>();
    }
    
    public static Class<?> wrap(Class<?> type) {
        if (type == null) return null;
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return type;
    }
    
    public void clean() {
        types.clear();
    }
    
    public boolean isEmpty() {
        return types.isEmpty();
    }
    
    public OpOrder getOrder() {
        if (order == null) return OpOrder.UNARY;
        return order;
    }
    
    public boolean isLeftToRight() {
        return leftToRight;
    }
    
    public void setLeftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
    }
    
    public boolean canContain(OperatorType type) {
        return types.stream().noneMatch(t -> type.getLeftWrap() == t.getLeftWrap() && type.getRightWrap() == t.getRightWrap());
    }
    
    public boolean insert(OperatorType action) {
        if (!action.isPlaceholder() && !canContain(action)) return false;
        if (order == null && action.getOrder() != OpOrder.UNARY) {
            order = action.getOrder();
            leftToRight = action.isLeftToRight();
        }
        types.add(action);
        return true;
    }
    
    public Optional<OperatorType> find(Object left, Object right) {
        return findAction(left, right);
    }
    
    public Optional<OperatorType<?, ?, ?>> find(Class<?> left, Class<?> right) {
        return types.stream().filter(t -> t.getLeftWrap() == left && t.getRightWrap() == right).findFirst();
    }
    
    private Optional<OperatorType> findAction(Object left, Object right) {
        types.forEach(type -> {
            if (type.isPlaceholder()) return;
            OperatorMatcher matcher = type.getMatcher();
            if (matcher == null) return;
            if (matcher.matches(type, left, right)) {
                search.add(type);
            }
        });
        if (search.size() == 0) return Optional.empty();
        if (search.size() > 1) {
            OperatorMatcher matcher = OperatorMatcher.sameType();
            search.removeIf(type -> !matcher.matches(type, left, right));
            if (search.size() != 1) return Optional.empty();
        }
        OperatorType<?, ?, ?> action = search.get(0);
        search.clear();
        return Optional.of(action);
    }
    
}
