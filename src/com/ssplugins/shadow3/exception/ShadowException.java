package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.List;

public class ShadowException extends RuntimeException {
    
    private String raw;
    private int line;
    private int pos;
    
    public ShadowException(String raw, int line, int pos) {
        this.raw = raw;
        this.line = line;
        this.pos = pos;
    }
    
    public ShadowException(String raw, int line, int pos, String message) {
        super(message);
        this.raw = raw;
        this.line = line;
        this.pos = pos;
    }
    
    public ShadowException(TokenLine tokenLine, int pos, String message) {
        this(tokenLine.getRaw(), tokenLine.getLine(), pos, message);
    }
    
    public ShadowException(String raw, int line, int pos, String message, Throwable cause) {
        super(message, cause);
        this.raw = raw;
        this.line = line;
        this.pos = pos;
    }
    
    public ShadowException(List<String> lines, int line, int pos, String message) {
        this(lines.get(line), line, pos, message);
    }
    
    public String getRaw() {
        return raw;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getPos() {
        return pos;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line ").append(line);
        if (pos > -1) builder.append(":").append(pos);
        builder.append("\n");
        builder.append(raw);
        builder.append("\n");
        if (pos > -1) {
            for (int i = 0; i < pos; ++i) {
                builder.append(" ");
            }
            builder.append("^");
        }
        return builder.toString();
    }
    
}
