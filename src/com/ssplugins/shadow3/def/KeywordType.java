package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

public class KeywordType {
    
    private String name;
    private Range arguments;
    private Schema<Keyword> schema;
    
    private ParseCallback<Keyword> parseCallback;
    private KeywordAction action;
    
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
