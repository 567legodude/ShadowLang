package com.ssplugins.shadow.lang;

import java.util.*;

public class Scope {
	
	private boolean block = false;
	private String blockName = null;
	private List<Variable> globalVars;
	private List<Variable> localVars = new ArrayList<>();
	private List<Variable> privateVars = new ArrayList<>();
	private UUID tempID;
	private Scope upper;
	private MsgCallback msgCallback;
	
	Scope(List<Variable> globalVars, Scope upper) {
		this.globalVars = globalVars;
		this.upper = upper;
		if (upper != null) msgCallback = upper.msgCallback;
	}
	
	public void clean() {
		localVars.clear();
	}
	
	void setMsgCallback(MsgCallback callback) {
		msgCallback = callback;
	}
	
	public void msg(String msg) {
		if (msgCallback != null) msgCallback.msg(msg);
	}
	
	public void error(String msg) {
		msg("Error: " + msg);
	}
	
	public void info(String msg) {
		msg("Info: " + msg);
	}
	
	public boolean isBlock() {
		return block;
	}
	
	public String getBlockName() {
		return blockName;
	}
	
	public Scope levelUp() {
		return upper;
	}
	
	public List<Variable> getAllLocalVars() {
		return new ArrayList<>(localVars);
	}
	
	public boolean isVarSet(String var) {
		return localVars.stream().anyMatch(variable -> variable.getName().equals(var));
	}
	
	public Optional<Variable> getGlobalVar(String name) {
		return globalVars.stream().filter(variable -> variable.getName().equals(name)).findFirst();
	}
	
	public Optional<Variable> getVar(String name) {
		return localVars.stream().filter(variable -> variable.getName().equals(name)).findFirst();
	}
	
	public Optional<Variable> getPrivateVar(String key) {
		return privateVars.stream().filter(variable -> variable.getName().equals(key)).findFirst();
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
	
	public String newPrivateVar(Object value) {
		tempID = UUID.randomUUID();
		while (privateVars.stream().anyMatch(variable -> variable.getName().equals(tempID.toString()))) {
			tempID = UUID.randomUUID();
		}
		privateVars.add(new Variable(tempID.toString(), value));
		return tempID.toString();
	}
	
	public void add(Variable variable) {
		setVar(variable.getName(), variable.getValue());
	}
	
	public void unset(String name) {
		localVars.removeIf(variable -> variable.getName().equalsIgnoreCase(name));
	}
	
	public void unsetG(String name) {
		globalVars.removeIf(variable -> variable.getName().equalsIgnoreCase(name));
	}
}
