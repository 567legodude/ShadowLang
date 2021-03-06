package com.ssplugins.shadow3.entity;

public class Flow {
    
    private ShadowEntity current;
    
    public Flow(ShadowEntity current) {
        this.current = current;
    }
    
    public ShadowEntity get() {
        return current;
    }
    
    public Flow next() {
        ShadowEntity next = current.getNext();
        if (next == null) return null;
        return next.flow();
    }
    
    public Flow previous() {
        ShadowEntity previous = current.getPrevious();
        if (previous == null) return null;
        return previous.flow();
    }
    
    public Flow parent() {
        ShadowEntity parent = current.getParent();
        if (parent == null) return null;
        return parent.flow();
    }
    
    public boolean isBlock(String... names) {
        if (!(current instanceof Block)) return false;
        if (names.length == 0) return true;
        String target = current.getName();
        for (String name : names) {
            if (target.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
    
    public boolean nextIsBlock(String... names) {
        Flow next = next();
        if (next == null) return false;
        return next.isBlock(names);
    }
    
    public boolean prevIsBlock(String... names) {
        Flow prev = previous();
        if (prev == null) return false;
        return prev.isBlock(names);
    }
    
    public boolean isKeyword(String... names) {
        if (!(current instanceof Keyword)) return false;
        if (names.length == 0) return true;
        String target = ((Keyword) current).getName();
        for (String name : names) {
            if (target.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
    
    public boolean nextIsKeyword(String... names) {
        Flow next = next();
        if (next == null) return false;
        return next.isKeyword(names);
    }
    
    public boolean prevIsKeyword(String... names) {
        Flow prev = previous();
        if (prev == null) return false;
        return prev.isKeyword(names);
    }
    
}
