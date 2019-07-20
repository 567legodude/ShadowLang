package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.BlockEffector;
import com.ssplugins.shadow3.compile.BlockGen;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

public class BlockType {
    
    private String name;
    private Range modifiers;
    private Range parameters;
    private Schema<Block> schema;
    
    private ParseCallback<Block> parseCallback;
    private PreRunCheck preRunCheck;
    private BlockEnterCallback enterCallback;
    private BlockEndCallback endCallback;
    private ParamLookup paramLookup;
    private BlockGen generator;
    private boolean effectsScope;
    private BlockEffector effector;
    
    private ContextTransformer<Block> contextTransformer = ContextTransformer.blockNone();
    private ShadowContext lookupContext;
    
    public BlockType(String name, Range modifiers, Range parameters) {
        this.name = name;
        this.modifiers = modifiers;
        this.parameters = parameters;
    }
    
    public String getName() {
        return name;
    }
    
    public Range getModifiers() {
        return modifiers;
    }
    
    public Range getParameters() {
        return parameters;
    }
    
    public Schema<Block> getSchema() {
        return schema;
    }
    
    public void setSchema(Schema<Block> schema) {
        this.schema = schema;
    }
    
    public ParseCallback<Block> getParseCallback() {
        return parseCallback;
    }
    
    public void setParseCallback(ParseCallback<Block> parseCallback) {
        this.parseCallback = parseCallback;
    }
    
    public PreRunCheck getPreRunCheck() {
        return preRunCheck;
    }
    
    public void setPreRunCheck(PreRunCheck preRunCheck) {
        this.preRunCheck = preRunCheck;
    }
    
    public BlockEnterCallback getEnterCallback() {
        return enterCallback;
    }
    
    public void setEnterCallback(BlockEnterCallback enterCallback) {
        this.enterCallback = enterCallback;
    }
    
    public BlockEndCallback getEndCallback() {
        return endCallback;
    }
    
    public void setEndCallback(BlockEndCallback endCallback) {
        this.endCallback = endCallback;
    }
    
    public ParamLookup getParamLookup() {
        return paramLookup;
    }
    
    public void setParamLookup(ParamLookup paramLookup) {
        this.paramLookup = paramLookup;
    }
    
    public BlockGen getGenerator() {
        return generator;
    }
    
    public void setGenerator(BlockGen generator) {
        this.generator = generator;
    }
    
    public boolean effectsScope() {
        return effectsScope;
    }
    
    public void setEffectsScope(boolean effectsScope) {
        this.effectsScope = effectsScope;
    }
    
    public BlockEffector getEffector() {
        return effector;
    }
    
    public void setEffector(BlockEffector effector) {
        this.effector = effector;
    }
    
    public ContextTransformer<Block> getContextTransformer() {
        return contextTransformer;
    }
    
    public void setContextTransformer(ContextTransformer<Block> contextTransformer) {
        this.contextTransformer = contextTransformer;
    }
    
    public ShadowContext getLookupContext() {
        return lookupContext;
    }
    
    public void setLookupContext(ShadowContext lookupContext) {
        this.lookupContext = lookupContext;
    }
    
}
