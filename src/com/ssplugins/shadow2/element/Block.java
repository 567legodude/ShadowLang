package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ParseContext;
import com.ssplugins.shadow2.ShadowTools;

import java.util.List;

public final class Block extends ShadowElement {
	
	private String name;
	private List<ShadowSection> modifiers;
	private List<Plain> parameters;
	private List<ShadowElement> content;
	
	public Block(ParseContext context, String name, List<ShadowSection> modifiers, List<Plain> parameters, List<ShadowElement> content) {
		super(context);
		this.name = name;
		this.modifiers = ShadowTools.lockList(modifiers);
		this.parameters = ShadowTools.lockList(parameters);
		this.content = ShadowTools.lockList(content);
	}
	
	public String getName() {
		return name;
	}
	
	public List<ShadowSection> getModifiers() {
		return modifiers;
	}
	
	public List<Plain> getParameters() {
		return parameters;
	}
	
	public List<ShadowElement> getContent() {
		return content;
	}
	
}
