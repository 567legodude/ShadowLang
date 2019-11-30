package com.ssplugins.shadow4.exception;

public class EOLException extends SourceCodeException {
    
    public EOLException(String line, int lineNumber, String expected) {
        super(line, lineNumber, line.length(), "Expected " + expected + " but reached end of line.");
    }
    
}
