package com.ssplugins.shadow.element;

import com.ssplugins.shadow.ShadowTools;

public class Reference extends ShadowSection {
	
	private Object value;
	
	public Reference(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return ShadowTools.get(value).map(Object::toString).orElse("null");
	}
	
}
