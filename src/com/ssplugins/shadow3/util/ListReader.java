package com.ssplugins.shadow3.util;

import java.util.List;

public class ListReader<T> extends Reader<T> {
    
    private List<T> list;
    
    public ListReader(List<T> list) {
        this.list = list;
    }
    
    @Override
    protected T get(int index) {
        return list.get(index);
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    public List<T> getList() {
        return list;
    }
    
}
