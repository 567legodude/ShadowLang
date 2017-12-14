package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ParseContext;

public abstract class ShadowElement {
	
	private String raw;
	private int line;
	
	public ShadowElement(ParseContext context) {
		raw = context.raw();
		line = context.getLine();
	}
	
	public boolean isBlock() {
		return this instanceof Block;
	}
	
	public boolean isLine() {
		return this instanceof Keyword;
	}
	
	public Block asBlock() {
		if (isBlock()) {
			return (Block) this;
		}
		throw new IllegalStateException("Object is not Block.");
	}
	
	public Keyword asLine() {
		if (isLine()) {
			return (Keyword) this;
		}
		throw new IllegalStateException("Object is not Line.");
	}

}
