package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

public class KeywordType {
    
    private String name;
    private Range arguments;
    private Schema<Keyword> schema;
    
    private KeywordAction action;
    
    private ContextTransformer<Keyword> contextTransformer = ContextTransformer.keywordNone();
    
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
    
}
