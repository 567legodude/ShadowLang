package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.Block;
import com.ssplugins.shadow2.element.ShadowElement;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Shadow {
	
	private List<ShadowElement> elements;
	private ParseContext context;
	private Executor executor;
	
	private Shadow(List<ShadowElement> elements) {
		this.elements = Collections.unmodifiableList(elements);
		this.executor = new Executor(this);
	}
	
	public static Shadow empty() {
		return new Shadow(Collections.emptyList());
	}
	
	public ParseContext getContext() {
		return context;
	}
	
	public List<ShadowElement> getElements() {
		return elements;
	}
	
	public List<Block> getBlocks() {
		return elements.stream().filter(ShadowElement::isBlock).map(ShadowElement::asBlock).collect(Collectors.toList());
	}
	
	public List<Block> getBlocks(String name) {
		return getBlocks().stream().filter(block -> block.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
	}
	
	public void run(Block block, Object... params) {
		//
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
