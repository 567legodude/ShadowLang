package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ParseContext;

import java.util.List;

public final class Block extends ShadowElement {
	
	private String name;
	private List<ShadowSection> modifiers;
	private List<Plain> parameters;
	private List<ShadowElement> content;
	
	public Block(ParseContext context, String name, List<ShadowSection> modifiers, List<Plain> parameters, List<ShadowElement> content) {
		super(context);
		this.name = name;
		this.modifiers = modifiers;
		this.parameters = parameters;
		this.content = content;
	}
	
}
