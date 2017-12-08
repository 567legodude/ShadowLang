package com.ssplugins.shadow2.element;

public abstract class ShadowElement {
	
	private String raw;
	private int number;
	
	public boolean isBlock() {
		return this instanceof Block;
	}
	
	public boolean isLine() {
		return this instanceof Line;
	}
	
	public Block asBlock() {
		if (isBlock()) {
			return (Block) this;
		}
		throw new IllegalStateException("Object is not Block.");
	}
	
	public Line asLine() {
		if (isLine()) {
			return (Line) this;
		}
		throw new IllegalStateException("Object is not Line.");
	}

}
