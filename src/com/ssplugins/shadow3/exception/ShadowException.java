package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.section.ShadowSection;

import java.util.List;
import java.util.function.Supplier;

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
        this(tokenLine.getRaw(), tokenLine.getLineNumber(), pos, message);
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
    
    public static ShadowParseError ended(TokenReader reader) {
        return new ShadowParseError(reader.getLine(), reader.getLine().lastToken().indexAfter(), "Unexpected end of line.");
    }
    
    public static Supplier<IllegalArgumentException> arg(String msg) {
        return () -> new IllegalArgumentException(msg);
    }
    
    public static Supplier<NamedShadowException> section(ShadowSection section, String type, String msg) {
        return () -> new NamedShadowException(type, section.getLine(), section.getPrimaryToken().getIndex(), msg);
    }
    
    public static Supplier<NamedShadowException> sectionExec(ShadowSection section, String msg) {
        return () -> new ShadowExecutionError(section.getLine(), section.getPrimaryToken().getIndex(), msg);
    }
    
    public static Supplier<NamedShadowException> noDef(TokenLine line, int index, String msg) {
        return () -> new NamedShadowException("DefinitionError", line, index, msg);
    }
    
    public static Supplier<NamedShadowException> noClose(TokenLine line, int index, String msg) {
        return () -> new NamedShadowException("EOFError", line, index, msg);
    }
    
    public static Supplier<ShadowExecutionError> exec(Block block, String msg) {
        return () -> new ShadowExecutionError(block.getLine(), block.getLine().firstToken().getIndex(), msg);
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
