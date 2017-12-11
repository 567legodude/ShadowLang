package com.ssplugins.shadow2.element;

import java.util.List;

public final class Line extends ShadowElement {
	
	private Plain keyword;
	private List<ShadowSection> arguments;
	
	public Line(Plain keyword, List<ShadowSection> arguments) {
		this.keyword = keyword;
		this.arguments = arguments;
	}
	
}
