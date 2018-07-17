package com.ssplugins.shadow.exceptions;

import com.ssplugins.shadow.ParseContext;

public class ShadowExecutionException extends ShadowException {
	
	public ShadowExecutionException(String message) {
		super(message);
	}
	
	public ShadowExecutionException(String message, int line) {
		super(message + " (Line " + line + ")");
	}
	
	public ShadowExecutionException(String message, ParseContext context) {
		super(message + " (Line " + context.getLine() + ": \"" + context.raw() + "\")");
	}
	
	public ShadowExecutionException(Throwable cause) {
		super(cause);
	}
	
}
