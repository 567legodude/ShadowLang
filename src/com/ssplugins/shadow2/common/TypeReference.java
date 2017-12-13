package com.ssplugins.shadow2.common;

public class TypeReference {
	
	private Object value;
	private Class<?> type;
	
	public TypeReference() {}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public void setType(Class<?> type) {
		this.type = type;
	}
	
}
