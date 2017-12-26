package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.NamedReference;

import java.util.Optional;

public class Scope {
	
	private ParseContext context;
	
	private Scope parent;
	private SectionList<NamedReference<Object>> vars = SectionList.create(NamedReference::getName);
	
	private Scope(SectionList<NamedReference<Object>> vars) {
		this.vars = vars;
	}
	
	public Scope(ParseContext context) {
		this.context = context;
	}
	
	public Scope parent() {
		return parent;
	}
	
	public Scope newChild() {
		Scope scope = new Scope(vars.subsection());
		scope.parent = this;
		scope.context = this.context;
		return scope;
	}
	
	public Optional<NamedReference<Object>> getVar(String name) {
		return vars.getFirst(name);
	}
	
	public void setVar(String name, Object value) {
		Optional<NamedReference<Object>> op = getVar(name);
		if (op.isPresent()) {
			op.get().set(value);
		}
		else {
			vars.add(new NamedReference<>(name, value));
		}
	}
	
	public void unset(String name) {
		getVar(name).ifPresent(vars::remove);
	}
	
	public ParseContext getContext() {
		return context;
	}
	
}
