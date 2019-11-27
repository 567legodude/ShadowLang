package com.ssplugins.shadow4.entity;

import com.ssplugins.shadow4.context.Context;
import com.ssplugins.shadow4.tokens.Line;

public abstract class Entity {
    
    private Context fromContext;
    
    private Entity parent, previous, next;
    private Line line;
    private int entityIndex;
    private boolean inline;
    
    private String name;
    // TODO List<Section> modifiers
    // TODO List<Parameter> parameters
    // TODO entity body
    // TODO definition
    // TODO inner context mechanism (as child of fromContext)
    // TODO interface that gets return type of entity
    
}
