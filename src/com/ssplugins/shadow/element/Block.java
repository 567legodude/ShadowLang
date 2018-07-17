package com.ssplugins.shadow.element;

import com.ssplugins.shadow.ParseContext;
import com.ssplugins.shadow.ShadowTools;

import java.util.List;

public final class Block extends ShadowElement {
	
	private String name;
	private List<ShadowSection> modifiers;
	private List<String> parameters;
	private List<ShadowElement> content;
	
	public Block(ParseContext context, String name, List<ShadowSection> modifiers, List<String> parameters, List<ShadowElement> content) {
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
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public List<ShadowElement> getContent() {
		return content;
	}
	
}
