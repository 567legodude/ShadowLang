package com.ssplugins.shadow4.util;

public enum Primitive {
    
    BOOLEAN(),
    BYTE(Short.class, Integer.class, Long.class, Float.class, Double.class),
    SHORT(Integer.class, Long.class, Float.class, Double.class),
    CHAR(Integer.class, Long.class, Float.class, Double.class),
    INT(Long.class, Float.class, Double.class),
    LONG(Float.class, Double.class),
    FLOAT(Double.class),
    DOUBLE();
    
    private Class[] wideCasts;
    
    Primitive(Class... wideCasts) {
        this.wideCasts = wideCasts;
    }
    
    public static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return type;
    }
    
    public static Primitive from(Class<?> type) {
        type = wrap(type);
        if (type == Boolean.class) return BOOLEAN;
        if (type == Byte.class) return BYTE;
        if (type == Short.class) return SHORT;
        if (type == Character.class) return CHAR;
        if (type == Integer.class) return INT;
        if (type == Long.class) return LONG;
        if (type == Float.class) return FLOAT;
        if (type == Double.class) return DOUBLE;
        return null;
    }
    
    public static boolean validCast(Class<?> from, Class<?> to) {
        Primitive p = Primitive.from(from);
        return p != null && p.canCastTo(to);
    }
    
    public static Object as(Object obj, Class<?> target) {
        if (obj.getClass() == target) return obj;
        Primitive p = from(obj.getClass());
        if (p == null) return target.cast(obj);
        if (!p.canCastTo(target))
            throw new ClassCastException("Cannot cast " + obj.getClass().getName() + " to " + target.getName());
        switch (p) {
            case BOOLEAN:
                return obj;
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return performCast((Number) obj, target);
            case CHAR:
                return performCast((int) (Character) obj, target);
            default:
                return target.cast(obj);
        }
    }
    
    private static Object performCast(Number n, Class<?> type) {
        type = wrap(type);
        if (type == byte.class) return n.byteValue();
        if (type == short.class) return n.shortValue();
        if (type == int.class) return n.intValue();
        if (type == long.class) return n.longValue();
        if (type == float.class) return n.floatValue();
        if (type == double.class) return n.doubleValue();
        return n;
    }
    
    public boolean canCastTo(Class<?> other) {
        other = wrap(other);
        for (Class cast : wideCasts) {
            if (cast == other) {
                return true;
            }
        }
        return false;
    }
    
    public Class[] getWideCasts() {
        return wideCasts;
    }
    
}
