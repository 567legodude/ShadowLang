package com.ssplugins.shadow.lang;

public interface StepperInfo {
	
	Block getBlock();
	
	Block getPrevBlock();
	
	boolean lastBlockRan();
	
	boolean lastBlockIs(String type);
	
}
