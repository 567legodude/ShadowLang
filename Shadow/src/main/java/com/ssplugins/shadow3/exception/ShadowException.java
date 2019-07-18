package com.ssplugins.shadow3.exception;

public class ShadowException extends RuntimeException {
    
    public ShadowException() {
    }
    
    public ShadowException(String message) {
        super(message);
    }
    
    public ShadowException(Throwable cause) {
        super(cause);
    }
    
    public ShadowException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
