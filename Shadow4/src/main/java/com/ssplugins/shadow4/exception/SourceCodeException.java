package com.ssplugins.shadow4.exception;

public class SourceCodeException extends RuntimeException {
    
    private String line;
    private int lineNumber, index;
    
    public SourceCodeException(String line, int lineNumber, int index) {
        this.line = line;
        this.lineNumber = lineNumber;
        this.index = index;
    }
    
    public SourceCodeException(String line, int lineNumber, int index, String message) {
        super(message);
        this.line = line;
        this.lineNumber = lineNumber;
        this.index = index;
    }
    
    public SourceCodeException(String line, int lineNumber, int index, String message, Throwable cause) {
        super(message, cause);
        this.line = line;
        this.lineNumber = lineNumber;
        this.index = index;
    }
    
    @Override
    public String getMessage() {
        String message = super.getMessage();
        StringBuilder builder = new StringBuilder();
        if (message != null) {
            builder.append(message);
        }
        builder.append("\nLine ").append(lineNumber);
        if (index > -1) {
            builder.append(":").append(index);
        }
        builder.append("\n").append(line).append("\n");
        if (index > -1) {
            for (int i = 0; i < index; i++) {
                builder.append(' ');
            }
            builder.append("^");
        }
        return builder.toString();
    }
    
}
