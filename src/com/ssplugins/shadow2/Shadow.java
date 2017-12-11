package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.ShadowElement;

import java.util.Collections;
import java.util.List;

public class Shadow {
	
	private List<ShadowElement> elements;
	
	private Shadow(List<ShadowElement> elements) {
		this.elements = Collections.unmodifiableList(elements);
	}
	
	public static Shadow empty() {
		return new Shadow(Collections.emptyList());
	}
	
}
