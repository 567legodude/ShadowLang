package com.ssplugins.shadow2.exceptions;

public class ShadowParseException extends ShadowException {
	
	public ShadowParseException(String message) {
		super(message);
	}
	
	public ShadowParseException(String message, int line) {
		super(message + " (Line " + line + ")");
	}
	
}
