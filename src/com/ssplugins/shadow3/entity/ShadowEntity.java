package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.parsing.TokenLine;

public abstract class ShadowEntity {
    
    private ShadowEntity previous;
    private ShadowEntity next;
    
    private TokenLine line;
    private Block parent;
    
    public ShadowEntity(TokenLine line, Block parent) {
        this.line = line;
        this.parent = parent;
    }
    
    ShadowEntity getPrevious() {
        return previous;
    }
    
    void setPrevious(ShadowEntity previous) {
        this.previous = previous;
    }
    
    ShadowEntity getNext() {
        return next;
    }
    
    void setNext(ShadowEntity next) {
        this.next = next;
    }
    
    public TokenLine getLine() {
        return line;
    }
    
    public Block getParent() {
        return parent;
    }
    
}
