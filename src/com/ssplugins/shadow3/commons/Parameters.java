package com.ssplugins.shadow3.commons;

import java.util.ArrayList;
import java.util.List;

public class Parameters {
    
    private List<Object> params;
    
    public Parameters() {
        params = new ArrayList<>(3);
    }
    
    public int size() {
        return params.size();
    }
    
    public void addParam(Object value) {
        params.add(value);
    }
    
    public List<Object> getParams() {
        return params;
    }
    
}
