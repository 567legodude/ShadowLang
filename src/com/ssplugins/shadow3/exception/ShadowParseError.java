package com.ssplugins.shadow3.exception;

import com.ssplugins.shadow3.parsing.TokenLine;

public class ShadowParseError extends NamedShadowException {
    
    public ShadowParseError(TokenLine tokenLine, int pos, String message) {
        super("ParseError", tokenLine, pos, message);
    }
    
}
