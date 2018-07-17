package com.ssplugins.shadow.common;

public enum ParseLevel {
	
	NORMAL,
	STRICT_BLOCK,
	STRICT_KEYWORD,
	STRICT_ALL;
	
	public boolean allStrict() {
		return this == STRICT_ALL;
	}
	
	public boolean strictKeywords() {
		return this == STRICT_KEYWORD || allStrict();
	}
	
	public boolean strictBlocks() {
		return this == STRICT_BLOCK || allStrict();
	}
	
}
