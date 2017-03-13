package com.ssplugins.shadow.lang;

public interface BlockEnterEvent {
	
	void trigger(Block block, Scope scope, Stepper stepper);
	
}
