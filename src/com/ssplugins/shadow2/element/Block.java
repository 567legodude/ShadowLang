package com.ssplugins.shadow2.element;

import java.util.List;

public final class Block extends ShadowElement {
	
	private Plain name;
	private List<ShadowSection> modifiers;
	
	public Block(Plain name, List<ShadowSection> modifiers) {
		this.name = name;
		this.modifiers = modifiers;
	}
	
}
