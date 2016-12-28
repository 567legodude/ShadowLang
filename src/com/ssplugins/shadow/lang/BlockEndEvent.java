package com.ssplugins.shadow.lang;

public interface BlockEndEvent {
	
	void trigger(Block block, Scope scope, Stepper stepper);
	
}
