package com.ssplugins.shadow.lang;

public interface Replacer {
	
	String replace(String text, Line line, Scope scope, Stepper stepper);
	
}
