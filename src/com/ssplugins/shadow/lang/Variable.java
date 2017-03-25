package com.ssplugins.shadow.lang;

public class Variable {
	
	private final String name;
	private Object value;
	
	public Variable(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	public static Variable temp(Object value) {
		return new Variable(null, value);
	}
	
	public Variable rename(String name) {
		return new Variable(name, value);
	}
	
	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public void match(Variable variable) {
		if (variable == null) value = null;
		else value = variable.getValue();
	}
}
