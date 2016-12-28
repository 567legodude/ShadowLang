package com.ssplugins.shadow.lang;

class VariableType {
	
	private String type;
	private String name;
	
	public VariableType(String part) {
		String[] p = part.split(":");
		type = p[0];
		name = p[1];
	}
	
	public VariableType(String type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
}
