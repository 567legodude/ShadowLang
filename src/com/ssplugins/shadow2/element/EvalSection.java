package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ShadowTools;

import java.util.List;

public class EvalSection extends ShadowSection {
	
	private String token;
	private String name;
	private List<ShadowSection> params;
	
	public EvalSection(String token, String name, List<ShadowSection> params) {
		this.token = token;
		this.name = name;
		this.params = params;
	}
	
	@Override
	public String toString() {
		return token + name + ShadowTools.asString(params);
	}
	
}
