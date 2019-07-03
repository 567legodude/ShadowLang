package com.ssplugins.shadow3.execute;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.section.Identifier;

import java.util.*;

public class Scope {
    
    private ShadowContext context;
    private Stepper stepper;
    private Scope parent;
    
    private Map<String, Object> variables = new HashMap<>();
    
    private Object blockValue;
    private Object returnValue;
    
    private List<Runnable> callbacks;
    
    private Scope(ShadowContext context, Stepper stepper, Scope parent) {
        this.context = context;
        this.stepper = stepper;
        this.parent = parent;
        
        if (parent == null) callbacks = new LinkedList<>();
    }
    
    public Scope(ShadowContext context, Stepper stepper) {
        this(context, stepper, null);
    }
    
    private Scope find(String key) {
        Scope s = this;
        do {
            if (s.variables.containsKey(key)) return s;
            s = s.parent;
        } while (s != null);
        return null;
    }
    
    private Scope findOrLocal(String key) {
        Scope s = find(key);
        if (s == null) return this;
        return s;
    }
    
    public void setLocal(Identifier identifier, Object value) {
        setLocal(identifier.getName(), value);
    }
    
    public void setLocal(String key, Object value) {
        variables.put(key, value);
    }
    
    public void set(Identifier identifier, Object value) {
        set(identifier.getName(), value);
    }
    
    public void set(String key, Object value) {
        findOrLocal(key).setLocal(key, value);
    }
    
    public void unsetLocal(String key) {
        variables.remove(key);
    }
    
    public void unset(String key) {
        findOrLocal(key).unsetLocal(key);
    }
    
    public void unsetAll(String key) {
        Scope s;
        while ((s = find(key)) != null) {
            s.unsetLocal(key);
        }
    }
    
    public Optional<Object> get(String key) {
        return Optional.ofNullable(find(key)).map(scope -> scope.variables.get(key));
    }
    
    public Scope makeLevel() {
        return makeLevel(stepper);
    }
    
    public Scope makeLevel(Stepper stepper) {
        return new Scope(context, stepper, this);
    }
    
    public void reset() {
        variables.clear();
    }
    
    public void clean() {
        reset();
        context = null;
        parent = null;
        blockValue = null;
        returnValue = null;
    }
    
    public void addCallback(Runnable runnable) {
        Scope s = this;
        while (s.parent != null) s = s.parent;
        s.callbacks.add(runnable);
    }
    
    public void runCallbacks() {
        Scope s = this;
        while (s.parent != null) s = s.parent;
        s.callbacks.forEach(Runnable::run);
        s.callbacks.clear();
    }
    
    public List<Runnable> getCallbacks() {
        Scope s = this;
        while (s.parent != null) s = s.parent;
        return s.callbacks;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
    public Stepper getStepper() {
        return stepper;
    }
    
    public Scope getParent() {
        return parent;
    }
    
    public Object getBlockValue() {
        return blockValue;
    }
    
    public void setBlockValue(Object blockValue) {
        this.blockValue = blockValue;
    }
    
    public Object getReturnValue() {
        return returnValue;
    }
    
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
    
}
