package com.ssplugins.shadow2.element;

import java.util.regex.Pattern;

public class LazyReplacers extends ShadowSection {
	
	public static final Pattern PATTERN = Pattern.compile("/u00a7\\w{8}(?:-\\w{4}){3}-\\w{12}");
	
	private String content;
	
	public LazyReplacers(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return content;
	}
	
}
