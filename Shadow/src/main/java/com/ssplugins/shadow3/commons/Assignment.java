package com.ssplugins.shadow3.commons;

public abstract class Assignment<T> {
    
    private T index;
    private Object value;
    
    public Assignment(T index, Object value) {
        this.index = index;
        this.value = value;
    }
    
    public T getIndex() {
        return index;
    }
    
    public Object getValue() {
        return value;
    }
    
}
