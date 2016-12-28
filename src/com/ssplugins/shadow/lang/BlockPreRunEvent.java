package com.ssplugins.shadow.lang;

public interface BlockPreRunEvent {
	
	boolean trigger(Block block, Scope scope, StepperInfo info);
	
}
