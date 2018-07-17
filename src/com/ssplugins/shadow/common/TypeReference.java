package com.ssplugins.shadow.common;

import com.ssplugins.shadow.ShadowTools;
import com.ssplugins.shadow.element.Reference;
import com.ssplugins.shadow.element.ShadowSection;

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
	
	public void set(Object o) {
		setValue(o);
		setType(ShadowTools.get(o).map(Object::getClass).orElse(null));
	}
	
	public ShadowSection toSection() {
		return new Reference(value);
	}
	
}
