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
		this.params = ShadowTools.lockList(params);
	}
	
	public String getToken() {
		return token;
	}
	
	public String getName() {
		return name;
	}
	
	public List<ShadowSection> getParams() {
		return params;
	}
	
	@Override
	public String toString() {
		return token + name + ShadowTools.asString(params);
	}
	
}
