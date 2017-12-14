package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ParseContext;

import java.util.List;

public final class Keyword extends ShadowElement {
	
	private String keyword;
	private List<ShadowSection> arguments;
	
	public Keyword(ParseContext context, String keyword, List<ShadowSection> arguments) {
		super(context);
		this.keyword = keyword;
		this.arguments = arguments;
	}
	
}
