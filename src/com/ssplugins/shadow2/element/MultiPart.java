package com.ssplugins.shadow2.element;

import java.util.List;

public final class MultiPart extends ShadowSection {
	
	private List<ShadowSection> parts;
	
	public MultiPart(List<ShadowSection> parts) {
		this.parts = parts;
	}
	
}
