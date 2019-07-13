package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenReader;

import java.util.function.Supplier;

public class ShadowParseError extends NamedShadowException {
    
    public ShadowParseError(TokenLine tokenLine, int pos, String message) {
        super("ParseError", tokenLine, pos, message);
    }
    
    public static Supplier<ShadowParseError> reader(TokenReader reader, int pos, String msg) {
        return () -> new ShadowParseError(reader.getLine(), pos, msg);
    }
    
}
