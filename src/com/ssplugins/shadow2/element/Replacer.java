package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ShadowTools;

import java.util.List;

public final class Replacer extends ShadowSection {
	
	private String token;
	private List<ShadowSection> content;
	
	public Replacer(String token, List<ShadowSection> content) {
		this.token = token;
		this.content = ShadowTools.lockList(content);
	}
	
	public String getToken() {
		return token;
	}
	
	public List<ShadowSection> getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return token + "{" + ShadowTools.asString(content) + "}";
	}
	
}
