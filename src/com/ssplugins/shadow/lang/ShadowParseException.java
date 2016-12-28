package com.ssplugins.shadow.lang;

public class ShadowParseException extends RuntimeException {
	
	public ShadowParseException() {
		super("There was a problem parsing Shadow code.");
	}
	
	public ShadowParseException(String message) {
		super("Could not parse Shadow code: " + message);
	}
}
