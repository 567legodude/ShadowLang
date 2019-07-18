package com.ssplugins.shadow3.util;

public class Value {
    
    private Object value;
    
    public Value(Object value) {
        this.value = value;
    }
    
    public Object value() {
        return value;
    }
    
    public Value setValue(Object value) {
        this.value = value;
        return this;
    }
    
}
