package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.parsing.TokenLine;

public abstract class ShadowEntity {
    
    private Flow flow;
    private ShadowEntity previous;
    private ShadowEntity next;
    
    private TokenLine line;
    private ShadowEntity parent;
    
    private boolean inline;
    
    public ShadowEntity(TokenLine line, ShadowEntity parent, boolean inline) {
        this.line = line;
        this.parent = parent;
        this.inline = inline;
        flow = new Flow(this);
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
    
    public Flow flow() {
        return flow;
    }
    
    public ShadowEntity getParent() {
        return parent;
    }
    
    public TokenLine getLine() {
        return line;
    }
    
    public boolean isInline() {
        return inline;
    }
    
}
