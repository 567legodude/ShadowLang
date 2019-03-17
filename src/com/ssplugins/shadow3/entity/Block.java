package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;

public class Block extends ShadowEntity {
    
    private String name;
    private ShadowSection[] modifiers;
    private Identifier[] parameters;
    private EntityList contents;
    
    // Action interface
    
    public Block(TokenLine line, Block parent, String name) {
        super(line, parent);
        this.name = name;
    }
    
    public Flow flow() {
        return new Flow(this);
    }
    
    public String getName() {
        return name;
    }
    
    public EntityList getContents() {
        return contents;
    }
    
}
