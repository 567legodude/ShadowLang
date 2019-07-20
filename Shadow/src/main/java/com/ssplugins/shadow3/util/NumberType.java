package com.ssplugins.shadow3.util;

public enum NumberType {
    
    BYTE(Byte.class, false),
    SHORT(Short.class, false),
    INT(Integer.class, false),
    LONG(Long.class, false),
    FLOAT(Float.class, true),
    DOUBLE(Double.class, true);
    
    private Class<?> type;
    private boolean floatingPoint;
    
    NumberType(Class<?> type, boolean floatingPoint) {
        this.type = type;
        this.floatingPoint = floatingPoint;
    }
    
    public static NumberType from(Class<?> type) {
        if (type == Byte.class) return BYTE;
        if (type == Short.class) return SHORT;
        if (type == Integer.class) return INT;
        if (type == Long.class) return LONG;
        if (type == Float.class) return FLOAT;
        if (type == Double.class) return DOUBLE;
        return null;
    }
    
    public static boolean isAssignableFrom(Class<?> a, Class<?> b) {
        NumberType t = from(a);
        if (t != null) return t.validValue(b);
        return a.isAssignableFrom(b);
    }
    
    public static boolean isIntegerType(Class<?> c) {
        NumberType t = from(c);
        return t != null && !t.isFloatingPoint();
    }
    
    public boolean validValue(Class<?> type) {
        if (this.type == type) return true;
        NumberType t = from(type);
        return t != null && (t.ordinal() < ordinal());
    }
    
    public Class<?> asMinimum(Class<?> type) {
        if (this.type == type) return type;
        NumberType t = from(type);
        if (t == null || t.ordinal() < ordinal()) return this.type;
        return type;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public boolean isFloatingPoint() {
        return floatingPoint;
    }
}
