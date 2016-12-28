package com.ssplugins.shadow.lang;

public class BooleanValue {
	
	private boolean value;
	
	public BooleanValue(boolean value) {
		this.value = value;
	}
	
	public void then(Runnable runnable) {
		if (!value) return;
		if (runnable != null) runnable.run();
	}
}
