package com.ssplugins.shadow.lang;

class Section {
	
	private boolean isBlock = false;
	private ShadowComponent component;
	
	Section(Block block) {
		isBlock = true;
		component = block;
	}
	
	Section(Line line) {
		isBlock = false;
		component = line;
	}
	
	Shadow getShadow() {
		return component.getShadow();
	}
	
	boolean isBlock() {
		return isBlock;
	}
	
	boolean isLine() {
		return !isBlock;
	}
	
	Block getBlock() {
		if (isBlock()) return (Block) component;
		return null;
	}
	
	Line getLine() {
		if (isLine()) return (Line) component;
		return null;
	}
}
