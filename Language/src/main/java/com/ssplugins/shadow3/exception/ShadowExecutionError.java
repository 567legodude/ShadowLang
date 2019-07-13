package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.List;

public class ShadowExecutionError extends NamedShadowException {
    
    public ShadowExecutionError(String raw, int line, int pos) {
        super("ExecutionError", raw, line, pos);
    }
    
    public ShadowExecutionError(String raw, int line, int pos, String message) {
        super("ExecutionError", raw, line, pos, message);
    }
    
    public ShadowExecutionError(TokenLine tokenLine, int pos, String message) {
        super("ExecutionError", tokenLine, pos, message);
    }
    
    public ShadowExecutionError(String raw, int line, int pos, String message, Throwable cause) {
        super("ExecutionError", raw, line, pos, message, cause);
    }
    
    public ShadowExecutionError(List<String> lines, int line, int pos, String message) {
        super("ExecutionError", lines, line, pos, message);
    }
    
}
