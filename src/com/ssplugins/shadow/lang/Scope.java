package com.ssplugins.shadow.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scope {
	
	private boolean block = false;
	private String blockName = null;
	private List<Variable> globalVars;
	private List<Variable> localVars = new ArrayList<>();
	
	Scope(List<Variable> globalVars) {
		this.globalVars = globalVars;
	}
	
	public void clean() {
		localVars.clear();
	}
	
	public boolean isBlock() {
		return block;
	}
	
	public String getBlockName() {
		return blockName;
	}
	
	public List<Variable> getAllLocalVars() {
		return new ArrayList<>(localVars);
	}
	
	public Optional<Variable> getGlobalVar(String name) {
		return globalVars.stream().filter(variable -> variable.getName().equals(name)).findFirst();
	}
	
	public Optional<Variable> getVar(String name) {
		return localVars.stream().filter(variable -> variable.getName().equals(name)).findFirst();
	}
	
	public void setGlobalVar(String name, Object value) {
		Optional<Variable> op = getGlobalVar(name);
		if (!op.isPresent()) {
			globalVars.add(new Variable(name, value));
			return;
		}
		op.get().setValue(value);
	}
	
	public void setVar(String name, Object value) {
		Optional<Variable> op = getVar(name);
		if (!op.isPresent()) {
			localVars.add(new Variable(name, value));
			return;
		}
		op.get().setValue(value);
	}
	
	public void unset(String name) {
		localVars.removeIf(variable -> variable.getName().equalsIgnoreCase(name));
	}
	
	public void unsetG(String name) {
		globalVars.removeIf(variable -> variable.getName().equalsIgnoreCase(name));
	}
}
