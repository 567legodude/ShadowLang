package com.ssplugins.shadow3.entity;

import java.util.Optional;

public class Flow {
    
    private ShadowEntity current;
    
    public Flow(ShadowEntity current) {
        this.current = current;
    }
    
    public Optional<ShadowEntity> get() {
        return Optional.of(current);
    }
    
    public Flow next() {
        return current.getNext().flow();
    }
    
    public Flow previous() {
        return current.getPrevious().flow();
    }
    
    public Flow parent() {
        return current.getParent().flow();
    }
    
    public boolean isBlock(String... names) {
        if (!(current instanceof Block)) return false;
        if (names.length == 0) return true;
        String target = ((Block) current).getName();
        for (String name : names) {
            if (target.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
    
}
