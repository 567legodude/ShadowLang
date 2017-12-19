package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ShadowTools;

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
