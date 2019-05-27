package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordAction;
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
import java.util.stream.Collectors;

public class Keyword extends ShadowEntity {
    
    private String name;
    private List<ShadowSection> arguments;
    
    private KeywordType definition;
    private ShadowContext innerContext;
    
    public Keyword(ShadowEntity parent, TokenReader def, ShadowContext fallback) {
        super(def.getLine(), parent);
        setTopContext((parent == null ? fallback : parent.getTopContext()));
        name = def.readAs(TokenType.IDENTIFIER).getPrimaryToken().getRaw();
    
        definition = findDef(parent, fallback);
        
        arguments = new ArrayList<>();
        while (def.hasNext()) {
            arguments.add(def.nextSection());
        }
        
        innerContext = definition.getContextTransformer().get(this, fallback, (getFrom() == null ? fallback : getFrom().getInnerContext()));
    }
    
    private KeywordType findDef(ShadowEntity parent, ShadowContext fallback) {
        while (parent != null) {
            ShadowContext context = parent.getInnerContext();
            if (context != null) {
                Optional<KeywordType> keyword = context.findKeyword(name);
                if (keyword.isPresent()) {
                    setFrom(parent);
                    return keyword.get();
                }
            }
            parent = parent.getParent();
        }
        return fallback.findKeyword(name).orElseThrow(ShadowException.noDef(getLine(), getLine().firstToken().getIndex(), "No definition found for keyword: " + name));
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        KeywordAction action = definition.getAction();
        if (action == null) {
            throw new ShadowException(getLine(), getLine().firstToken().getIndex(), "Keyword has no defined action.");
        }
        return action.execute(this, stepper, scope);
    }
    
    @Override
    public void addArgument(ShadowSection section) {
        arguments.add(section);
    }
    
    @Override
    public List<ShadowSection> getArguments() {
        return arguments;
    }
    
    @Override
    public ShadowContext getInnerContext() {
        return innerContext;
    }
    
    public List<Object> argumentValues(Scope scope) {
        return getArguments().stream().map(section -> section.toObject(scope)).collect(Collectors.toList());
    }
    
    public KeywordType getDefinition() {
        return definition;
    }
    
}
