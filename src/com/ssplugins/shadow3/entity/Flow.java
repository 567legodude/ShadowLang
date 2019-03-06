package com.ssplugins.shadow3.entity;

import java.util.Optional;

public class Flow {
    
    private ShadowEntity current;
    
    public Flow(ShadowEntity current) {
        this.current = current;
    }
    
    public Optional<ShadowEntity> get() {
        return Optional.ofNullable(current);
    }
    
    public Flow next() {
        if (current != null) current = current.getNext();
        return this;
    }
    
    public Flow previous() {
        if (current != null) current = current.getPrevious();
        return this;
    }
    
    public boolean isBlock(String... names) {
        if (current == null) return false;
        if (!(current instanceof Block)) return false;
        if (names.length == 0) return true;
        Block block = (Block) current;
        for (String name : names) {
            if (block.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
    
}
