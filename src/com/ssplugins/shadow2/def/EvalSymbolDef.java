package com.ssplugins.shadow2.def;

public final class EvalSymbolDef {
	
	private String token;
	private EvalAction action;
	
	public EvalSymbolDef(String token, EvalAction action) {
		this.token = token;
		this.action = action;
	}
	
	public String getToken() {
		return token;
	}
	
	public EvalAction getAction() {
		return action;
	}
	
}
