package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Schema;

import java.util.List;
import java.util.function.Supplier;

public class ShadowCodeException extends ShadowException {
    
    private String raw;
    private int line;
    private int pos;
    
    public ShadowCodeException(String raw, int line, int pos) {
        this.raw = raw;
        this.line = line;
        this.pos = pos;
    }
    
    public ShadowCodeException(String raw, int line, int pos, String message) {
        super(message);
        this.raw = raw;
        this.line = line;
        this.pos = pos;
    }
    
    public ShadowCodeException(TokenLine tokenLine, int pos, String message) {
        this(tokenLine.getRaw(), tokenLine.getLineNumber(), pos, message);
    }
    
    public ShadowCodeException(String raw, int line, int pos, String message, Throwable cause) {
        super(message, cause);
        this.raw = raw;
        this.line = line;
        this.pos = pos;
    }
    
    public ShadowCodeException(List<String> lines, int line, int pos, String message) {
        this(lines.get(line), line, pos, message);
    }
    
    public static ShadowParseError ended(TokenReader reader) {
        if (reader.getLimit() != -1) {
            return new ShadowParseError(reader.getLine(), reader.getLine().getTokens().get(reader.getIndex()).getIndex(), "Expected different token here.");
        }
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
    
    public static Supplier<NamedShadowException> schema(TokenLine line, int index, Schema schema) {
        return () -> new NamedShadowException("SchemaError", line, index, schema.getSituation());
    }
    
    public static Supplier<ShadowExecutionError> exec(Block block, String msg) {
        return () -> new ShadowExecutionError(block.getLine(), block.argumentIndex(-1), msg);
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
