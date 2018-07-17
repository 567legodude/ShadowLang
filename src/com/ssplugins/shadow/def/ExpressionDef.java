package com.ssplugins.shadow.def;

import java.util.function.Predicate;

public class ExpressionDef {
	
	private String token;
	private ExpressionAction action;
	
	public ExpressionDef(String token, ExpressionAction action) {
		this.token = token;
		this.action = action;
	}
	
	public static Predicate<ExpressionDef> is(String token) {
		return expressionDef -> expressionDef.getToken().equals(token);
	}
	
	public String getToken() {
		return token;
	}
	
	public ExpressionAction getAction() {
		return action;
	}
	
}
