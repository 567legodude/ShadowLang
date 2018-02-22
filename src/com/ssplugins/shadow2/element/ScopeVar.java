package com.ssplugins.shadow2.element;

public class ScopeVar extends ShadowSection {
	
	private String var;
	
	public ScopeVar(String var) {
		this.var = var;
	}
	
	public String getVar() {
		return var;
	}
	
	@Override
	public String toString() {
		return "[" + var + "]";
	}
	
}
