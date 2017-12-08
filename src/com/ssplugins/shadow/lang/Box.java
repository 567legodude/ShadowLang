package com.ssplugins.shadow.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class Box {
	
	private Map<Variable, Class<?>> vars = new HashMap<>();
	
	public Box() {}
	
	public void newVar(String name, Class<?> type) {
		newVar(name, type, null);
	}
	
	public void newVar(String name, Class<?> type, Object value) {
		vars.put(new Variable(name, value), type);
	}
	
	private Entry<Variable, Class<?>> getRaw(String name) {
		for (Entry<Variable, Class<?>> entry : vars.entrySet()) {
			if (entry.getKey().getName().equals(name)) return entry;
		}
		return null;
	}
	
	public Optional<Variable> getVar(String name) {
		Entry<Variable, Class<?>> entry = getRaw(name);
		if (entry == null) return Optional.empty();
		return Optional.of(entry.getKey());
	}
	
	public void setVar(String name, Object value) {
		Entry<Variable, Class<?>> entry = getRaw(name);
		if (entry == null) return;
		if (!entry.getValue().isAssignableFrom(value.getClass())) {
			Debugger.log(entry.getValue().getSimpleName() + " is not assignable from " + value.getClass().getSimpleName());
			return;
		}
		entry.getKey().setValue(value);
	}

}
