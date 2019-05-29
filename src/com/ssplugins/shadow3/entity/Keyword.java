package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordAction;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.ParseCallback;
import com.ssplugins.shadow3.exception.NamedShadowException;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Range;

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
        setTopContext((parent == null ? fallback : parent.getTopContext()));
        name = def.readAs(TokenType.IDENTIFIER).getPrimaryToken().getRaw();
    
        definition = findDef(parent, fallback);
        
        arguments = new ArrayList<>();
        while (def.hasNext()) {
            arguments.add(def.nextSection());
        }
    
        Range args = definition.getArguments();
        if (!args.contains(arguments.size())) {
            throw new NamedShadowException("", getLine(), getLine().firstToken().getIndex(), "Keyword expects " + args.toString("argument") + ", found " + arguments.size());
        }
    
        ParseCallback<Keyword> parseCallback = definition.getParseCallback();
        if (parseCallback != null) parseCallback.onParse(this, getEffectiveContext());
        innerContext = definition.getContextTransformer().get(this, fallback, getEffectiveContext());
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
    
    public KeywordType getDefinition() {
        return definition;
    }
    
}
