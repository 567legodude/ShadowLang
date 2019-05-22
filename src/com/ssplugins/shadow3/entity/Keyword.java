package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.List;

public class Keyword extends ShadowEntity {
    
    public Keyword(TokenLine line, ShadowEntity parent) {
        super(line, parent);
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        return null;
    }
    
}
