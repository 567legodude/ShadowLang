package com.ssplugins.shadow.exceptions;

import com.ssplugins.shadow.ParseContext;

import java.util.function.Supplier;

public abstract class ShadowException extends RuntimeException {
	
	public ShadowException(String message) {
		super(message);
	}
	
	public ShadowException(Throwable cause) {
		super(cause);
	}
	
	public static Supplier<ShadowException> err(String msg, int line) {
		return () -> new ShadowExecutionException(msg, line);
	}
	
	public static Supplier<ShadowException> err(String msg) {
        return () -> new ShadowExecutionException(msg);
    }
    
    public static Supplier<ShadowException> err(String msg, ParseContext context) {
        return () -> new ShadowExecutionException(msg, context);
    }
	
	public static Supplier<ShadowException> sectionConvert() {
        return err("Section could not be converted to object.");
    }
    
    public static Supplier<ShadowException> sectionConvert(ParseContext context) {
        return err("Section could not be converted to object.", context);
    }
	
}
