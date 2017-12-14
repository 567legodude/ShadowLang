package com.ssplugins.shadow2.exceptions;

import com.ssplugins.shadow2.ParseContext;

public class ShadowParseException extends ShadowException {
	
	public ShadowParseException(String message) {
		super(message);
	}
	
	public ShadowParseException(String message, int line) {
		super(message + " (Line " + line + ")");
	}
	
	public ShadowParseException(String message, ParseContext context) {
		super(message + " (Line " + context.getLine() + ": \"" + context.raw() + "\")");
	}
	
}
