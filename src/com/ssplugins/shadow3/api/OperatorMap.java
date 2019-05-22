package com.ssplugins.shadow3.api;

import com.ssplugins.shadow3.def.OperatorAction;
import com.ssplugins.shadow3.section.Operator.OpOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OperatorMap {
    
    private OpOrder order;
    private Map<Class<?>, Map<Class<?>, OperatorAction>> actions;
    
    public OperatorMap(OpOrder order) {
        this.order = order;
        actions = new HashMap<>();
    }
    
    private Map<Class<?>, OperatorAction> getMap(Class<?> type) {
        return actions.computeIfAbsent(type, c -> new HashMap<>(3));
    }
    
    private Class<?> wrap(Class<?> type) {
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
        actions.forEach((c, map) -> map.clear());
        actions.clear();
    }
    
    public boolean isEmpty() {
        return actions.isEmpty();
    }
    
    public OpOrder getOrder() {
        return order;
    }
    
    public boolean canContain(OperatorAction action) {
        return !find(action.getLeftType(), action.getRightType()).isPresent();
    }
    
    public boolean insert(OperatorAction action) {
        if (!action.isPlaceholder() && !canContain(action)) return false;
        getMap(wrap(action.getLeftType())).put(wrap(action.getRightType()), action);
        return true;
    }
    
    public Optional<OperatorAction> find(Class<?> left, Class<?> right) {
        return findAction(wrap(left), wrap(right));
    }
    
    private Optional<OperatorAction> findAction(Class<?> left, Class<?> right) {
        return Optional.ofNullable(actions.get(left)).map(types -> types.get(right));
    }
    
}
