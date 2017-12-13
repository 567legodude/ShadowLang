package com.ssplugins.shadow2.element;

import java.util.List;

public final class Keyword extends ShadowElement {
	
	private String keyword;
	private List<ShadowSection> arguments;
	
	public Keyword(String keyword, List<ShadowSection> arguments) {
		this.keyword = keyword;
		this.arguments = arguments;
	}
	
}
