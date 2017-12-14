package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.ShadowElement;

import java.util.Collections;
import java.util.List;

public class Shadow {
	
	private List<ShadowElement> elements;
	private ParseContext context;
	
	private Shadow(List<ShadowElement> elements) {
		this.elements = Collections.unmodifiableList(elements);
	}
	
	public static Shadow empty() {
		return new Shadow(Collections.emptyList());
	}
	
	public List<ShadowElement> getElements() {
		return elements;
	}
	
	public static class ShadowBuilder {
		
		private List<ShadowElement> elements;
		private ParseContext context;
		
		public ShadowBuilder() {}
		
		public ShadowBuilder elements(List<ShadowElement> elements) {
			this.elements = elements;
			return this;
		}
		
		public ShadowBuilder context(ParseContext context) {
			this.context = context;
			return this;
		}
		
		public Shadow build() {
			Shadow shadow = new Shadow(elements);
			shadow.context = context;
			return shadow;
		}
		
	}
	
}
