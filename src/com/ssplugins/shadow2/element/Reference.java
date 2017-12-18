package com.ssplugins.shadow2.element;

public class Reference extends ShadowSection {
	
	private Object value;
	
	public Reference(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	
}
