package com.ssplugins.shadow3.commons;

public class Assignment {
    
    private int index;
    private Object value;
    
    public Assignment(int index, Object value) {
        this.index = index;
        this.value = value;
    }
    
    public int getIndex() {
        return index;
    }
    
    public Object getValue() {
        return value;
    }
    
}
