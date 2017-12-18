package com.ssplugins.shadow2.common;

import java.util.concurrent.atomic.AtomicReference;

public class NamedReference<T> extends AtomicReference<T> {
	
	private final String name;
	
	public NamedReference(String name, T initialValue) {
		super(initialValue);
		this.name = name;
	}
	
	public NamedReference(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
