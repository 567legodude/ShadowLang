package com.ssplugins.shadow3.util;

import java.util.function.Supplier;

public abstract class Reader<T> {
    
    private int index;
    private int limit = -1;
    
    protected abstract T get(int index);
    
    public abstract int size();
    
    public void reset() {
        limit = -1;
    }
    
    public void consume() {
        consume(() -> new IndexOutOfBoundsException("Reader is exhausted."));
    }
    
    public void consume(Supplier<? extends RuntimeException> failCause) {
        if (!hasNext()) throw failCause.get();
        ++index;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public boolean hasNext() {
        return index < size() && (limit == -1 || index < limit);
    }
    
    public T next() {
        if (index == limit) throw new IndexOutOfBoundsException("Tried to read past limit.");
        return get(index++);
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
    
}
