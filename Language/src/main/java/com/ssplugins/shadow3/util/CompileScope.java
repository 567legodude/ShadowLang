package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.api.ShadowContext;

import java.util.*;

public class CompileScope {
    
    private ShadowContext context;
    private CompileScope parent;
    
    private Map<String, Class<?>> types = new HashMap<>();
    private Set<String> marks = new HashSet<>();
    private int temp = 0;
    
    public CompileScope(ShadowContext context) {
        this.context = context;
    }
    
    private CompileScope(ShadowContext context, CompileScope parent) {
        this(context);
        this.parent = parent;
        this.temp = parent.temp;
    }
    
    private CompileScope find(String key) {
        CompileScope s = this;
        while (s != null && !s.types.containsKey(key)) {
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
        return marks.contains(s);
    }
    
    public void mark(String s) {
        marks.add(s);
    }
    
    public String nextTemp() {
        return "_tmp" + temp++;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
}
