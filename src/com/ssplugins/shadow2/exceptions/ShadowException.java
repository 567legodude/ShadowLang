package com.ssplugins.shadow2.exceptions;

public abstract class ShadowException extends RuntimeException {
	
	public ShadowException(String message) {
		super(message);
	}
	
	public ShadowException(Throwable cause) {
		super(cause);
	}
	
}
