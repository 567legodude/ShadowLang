package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.section.Identifier;

public class Parameter {
    
    private Identifier identifier;
    private Class<?> type;
    
    public Parameter(Identifier identifier, Class<?> type) {
        this.identifier = identifier;
        this.type = type;
    }
    
    public Parameter(Identifier identifier) {
        this(identifier, Object.class);
    }
    
    public String getName() {
        return identifier.getName();
    }
    
    public Identifier getIdentifier() {
        return identifier;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public void setType(Class<?> type) {
        this.type = type;
    }
    
}
