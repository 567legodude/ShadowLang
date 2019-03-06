package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.List;

public class NamedShadowException extends ShadowException {
    
    private String name;
    
    public NamedShadowException(String name, String raw, int line, int pos) {
        super(raw, line, pos);
        this.name = name;
    }
    
    public NamedShadowException(String name, String raw, int line, int pos, String message) {
        super(raw, line, pos, message);
        this.name = name;
    }
    
    public NamedShadowException(String name, TokenLine tokenLine, int pos, String message) {
        super(tokenLine, pos, message);
        this.name = name;
    }
    
    public NamedShadowException(String name, String raw, int line, int pos, String message, Throwable cause) {
        super(raw, line, pos, message, cause);
        this.name = name;
    }
    
    public NamedShadowException(String name, List<String> lines, int line, int pos, String message) {
        super(lines, line, pos, message);
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name + ": " + getMessage() + "\n" + super.toString();
    }
    
}
