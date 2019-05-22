package com.ssplugins.shadow3.def;

import java.util.List;

public class BlockType {
    
    private String name;
    private int parameters;
    private ArgumentReader reader;
    
    private PreRunCheck preRunCheck;
    private BlockEnterCallback enterCallback;
    private BlockEndCallback endCallback;
    
    private List<KeywordType> localKeywords;
    
    public BlockType(String name, int parameters) {
        this.name = name;
        this.parameters = parameters;
    }
    
    public String getName() {
        return name;
    }
    
    public int getParameters() {
        return parameters;
    }
    
    public ArgumentReader getReader() {
        return reader;
    }
    
    public void setReader(ArgumentReader reader) {
        this.reader = reader;
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
    
    public List<KeywordType> getLocalKeywords() {
        return localKeywords;
    }
    
    public void setLocalKeywords(List<KeywordType> localKeywords) {
        this.localKeywords = localKeywords;
    }
    
}
