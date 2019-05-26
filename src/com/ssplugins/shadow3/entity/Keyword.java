package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.section.ShadowSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Keyword extends ShadowEntity {
    
    private String name;
    private List<ShadowSection> arguments;
    
    private KeywordType definition;
    private ShadowContext innerContext;
    
    public Keyword(ShadowEntity parent, TokenReader def, ShadowContext fallback) {
        super(def.getLine(), parent);
        name = def.readAs(TokenType.IDENTIFIER).getPrimaryToken().getRaw();
    
        definition = findDef(parent, fallback);
        
        arguments = new ArrayList<>();
        while (def.hasNext()) {
            arguments.add(def.nextSection());
        }
    
        innerContext = definition.getContextTransformer().get(this, fallback, parent.getInnerContext());
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        // TODO execute keyword
        return null;
    }
    
    @Override
    public void addArgument(ShadowSection section) {
        arguments.add(section);
    }
    
    @Override
    public ShadowContext getInnerContext() {
        return innerContext;
    }
    
    private KeywordType findDef(ShadowEntity parent, ShadowContext fallback) {
        while (parent != null) {
            ShadowContext context = parent.getInnerContext();
            Optional<KeywordType> keyword = context.findKeyword(name);
            if (keyword.isPresent()) return keyword.get();
            parent = parent.getParent();
        }
        return fallback.findKeyword(name).orElseThrow(ShadowException.noDef(getLine(), getLine().firstToken().getIndex(), "No definition found for keyword: " + name));
    }
    
    public List<ShadowSection> getArguments() {
        return arguments;
    }
    
    public KeywordType getDefinition() {
        return definition;
    }
    
}
