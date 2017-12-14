package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ShadowTools;

import java.util.List;

public final class Replacer extends ShadowSection {
	
	private String token;
	private List<ShadowSection> content;
	
	public Replacer(String token, List<ShadowSection> content) {
		this.token = token;
		this.content = content;
	}
	
	@Override
	public String toString() {
		return token + "{" + ShadowTools.asString(content) + "}";
	}
	
}
