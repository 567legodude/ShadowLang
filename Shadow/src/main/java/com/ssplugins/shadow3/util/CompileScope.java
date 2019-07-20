package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.api.ShadowContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CompileScope {
    
    private ShadowContext context;
    private CompileScope parent;
    
    private Map<String, Class<?>> types = new HashMap<>();
    private Set<String> marks = new HashSet<>();
    private AtomicInteger globalTemp;
    private int temp = 0;
    
    public CompileScope(ShadowContext context) {
        this.context = context;
        globalTemp = new AtomicInteger(0);
    }
    
    private CompileScope(ShadowContext context, CompileScope parent) {
        this.context = context;
        this.parent = parent;
        this.temp = parent.temp;
        globalTemp = parent.globalTemp;
    }
    
    private CompileScope find(String key) {
        CompileScope s = this;
        while (s != null && !s.types.containsKey(key)) {
            s = s.parent;
        }
        return s;
    }
    
    private CompileScope findMark(String key) {
        CompileScope s = this;
        while (s != null && !s.marks.contains(key)) {
            s = s.parent;
        }
        return s;
    }
    
    public CompileScope newBlock() {
        return new CompileScope(context, this);
    }
    
    public CompileScope parent() {
        return this.parent;
    }
    
    public boolean contains(String key) {
        return find(key) != null;
    }
    
    public Optional<Class<?>> get(String key) {
        return Optional.ofNullable(find(key)).map(scope -> scope.types.get(key));
    }
    
    public Pair<Boolean, Class<?>> addCheck(String key, Class<?> value) {
        Optional<Class<?>> op = get(key);
        if (op.isPresent()) {
            Class<?> type = op.get();
            if (type.isAssignableFrom(value)) return new Pair<>(true, type);
            return new Pair<>(false, type);
        }
        types.put(key, value);
        return new Pair<>(true, value);
    }
    
    public boolean isMarked(String s) {
        return findMark(s) != null;
    }
    
    public void mark(String s) {
        marks.add(s);
    }
    
    public String nextTemp() {
        return "$" + temp++;
    }
    
    public String nextGlobalTemp() {
        return "$" + globalTemp.getAndIncrement();
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
}
