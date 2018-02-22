package com.ssplugins.shadow2.common;

import com.ssplugins.shadow2.KeyedList;

import java.util.Optional;

public class Box {
	
	// Probably going to find better solution to this kind of thing.
	
	private KeyedList<NamedReference<Object>> list = new KeyedList<>(NamedReference::getName);
	
	public Optional<Object> getVar(String name) {
		if (!list.hasKey(name)) return Optional.empty();
		return Optional.of(list.getFirst(name).get());
	}
	
	public void setVar(String name, Object value) {
		NamedReference<Object> ref = list.getFirst(name);
		if (ref == null) {
			ref = new NamedReference<>(name);
			list.add(ref);
		}
		ref.set(value);
	}
	
}
