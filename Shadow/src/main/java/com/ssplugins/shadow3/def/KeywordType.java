package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.KeywordEffector;
import com.ssplugins.shadow3.compile.KeywordGen;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

public class KeywordType {
    
    private String name;
    private Range arguments;
    private Schema<Keyword> schema;
    
    private ParseCallback<Keyword> parseCallback;
    private KeywordAction action;
    private Returnable returnable = Returnable.none();
    private KeywordGen generator;
    private boolean statementMode;
    private boolean effectsScope;
    private KeywordEffector effector;
    
    private ContextTransformer<Keyword> contextTransformer = ContextTransformer.keywordNone();
    private ShadowContext lookupContext;
    
    public KeywordType(String name, Range arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    public String getName() {
        return name;
    }
    
    public Range getArguments() {
        return arguments;
    }
    
    public Schema<Keyword> getSchema() {
        return schema;
    }
    
    public void setSchema(Schema<Keyword> schema) {
        this.schema = schema;
    }
    
    public ParseCallback<Keyword> getParseCallback() {
        return parseCallback;
    }
    
    public void setParseCallback(ParseCallback<Keyword> parseCallback) {
        this.parseCallback = parseCallback;
    }
    
    public KeywordAction getAction() {
        return action;
    }
    
    public void setAction(KeywordAction action) {
        this.action = action;
    }
    
    public Returnable getReturnable() {
        return returnable;
    }
    
    public void setReturnable(Returnable returnType) {
        this.returnable = returnType;
    }
    
    public KeywordGen getGenerator() {
        return generator;
    }
    
    public void setGenerator(KeywordGen generator) {
        this.generator = generator;
    }
    
    public boolean isStatementMode() {
        return statementMode;
    }
    
    public void setStatementMode(boolean statementMode) {
        this.statementMode = statementMode;
    }
    
    public boolean effectsScope() {
        return effectsScope;
    }
    
    public void setEffectsScope(boolean effectsScope) {
        this.effectsScope = effectsScope;
    }
    
    public KeywordEffector getEffector() {
        return effector;
    }
    
    public void setEffector(KeywordEffector effector) {
        this.effector = effector;
    }
    
    public ContextTransformer<Keyword> getContextTransformer() {
        return contextTransformer;
    }
    
    public void setContextTransformer(ContextTransformer<Keyword> contextTransformer) {
        this.contextTransformer = contextTransformer;
    }
    
    public ShadowContext getLookupContext() {
        return lookupContext;
    }
    
    public void setLookupContext(ShadowContext lookupContext) {
        this.lookupContext = lookupContext;
    }
    
}
