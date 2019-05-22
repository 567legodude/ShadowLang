package com.ssplugins.shadow3.util;

public abstract class Reader<T> {
    
    private int increment = 1;
    private int index;
    private int limit = -1;
    
    protected abstract T get(int index);
    
    protected abstract int size();
    
    public void reset() {
        increment = 1;
        limit = -1;
    }
    
    public void consume() {
        if (!hasNext()) throw new IndexOutOfBoundsException("Reader is exhausted.");
        index += increment;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public boolean hasNext() {
        return index > -1 && index < size() && (limit == -1 || index < limit);
    }
    
    public T next() {
        T t = get(index);
        index += increment;
        return t;
    }
    
    public T peekNext() {
        return get(index);
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public int getIncrement() {
        return increment;
    }
    
    public void setIncrement(int increment) {
        this.increment = increment;
    }
    
}
