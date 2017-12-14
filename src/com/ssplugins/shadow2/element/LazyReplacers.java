package com.ssplugins.shadow2.element;

public class LazyReplacers extends ShadowSection {
	
	private String content;
	
	public LazyReplacers(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return content;
	}
	
}
