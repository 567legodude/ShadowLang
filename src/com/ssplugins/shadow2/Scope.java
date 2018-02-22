package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.NamedReference;

import java.util.Optional;

public class Scope {
	
	private ParseContext context;
	
	private Scope parent;
	private SectionList<NamedReference<Object>> vars = SectionList.create(NamedReference::getName);
	private SectionList<NamedReference<Object>> pVars = SectionList.create(NamedReference::getName);
	
	private Scope(SectionList<NamedReference<Object>> vars) {
		this.vars = vars;
	}
	
	public Scope(ParseContext context) {
		this.context = context;
	}
	
	public Scope parent() {
		return parent;
	}
	
	public int level() {
		int i = 1;
		Scope p = parent;
		while (p != null) {
			i++;
			p = p.parent;
		}
		return i;
	}
	
	public void clearScope() {
		vars.clearSection();
	}
	
	public Scope newChild() {
		Scope scope = new Scope(vars.subsection());
		scope.parent = this;
		scope.context = this.context;
		return scope;
	}
	
	public Optional<NamedReference<Object>> getVar(String name) {
		Optional<NamedReference<Object>> pop = pVars.getFirst(name);
		if (pop.isPresent()) return pop;
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
	
	public void setParamVar(String name, Object value) {
		Optional<NamedReference<Object>> op = getVar(name);
		if (op.isPresent()) {
			op.get().set(value);
		}
		else {
			pVars.add(new NamedReference<>(name, value));
		}
	}
	
	public void unset(String name) {
		getVar(name).ifPresent(ref -> {
			vars.remove(ref);
			pVars.remove(ref);
		});
	}
	
	public ParseContext getContext() {
		return context;
	}
	
}
