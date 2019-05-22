package com.ssplugins.shadow3.def;

public class KeywordType {
    
    private String name;
    private ArgumentReader reader;
    
    private KeywordAction action;
    
    public KeywordType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public ArgumentReader getReader() {
        return reader;
    }
    
    public void setReader(ArgumentReader reader) {
        this.reader = reader;
    }
    
    public KeywordAction getAction() {
        return action;
    }
    
    public void setAction(KeywordAction action) {
        this.action = action;
    }
    
}
