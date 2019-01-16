package com.ssplugins.shadow;

import com.ssplugins.shadow.common.NamedReference;

import java.util.Optional;

public class Scope {
	
    private Shadow shadow;
	private ParseContext context;
	
	private Scope parent;
	private SectionList<NamedReference<Object>> vars = SectionList.create(NamedReference::getName);
	
	private Scope(SectionList<NamedReference<Object>> vars) {
		this.vars = vars;
	}
	
	public Scope(Shadow shadow, ParseContext context) {
	    this.shadow = shadow;
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
    
    public Shadow getShadow() {
        return shadow;
    }
    
    public void clearScope() {
		vars.clearSection();
	}
	
	public Scope newChild() {
		Scope scope = new Scope(vars.subsection());
		scope.parent = this;
		scope.context = this.context;
        scope.shadow = shadow;
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
	
	public void setParamVar(String name, Object value) {
		Optional<NamedReference<Object>> op = getVar(name);
		if (op.isPresent()) {
			op.get().set(value);
		}
		else {
			vars.addSecondary(new NamedReference<>(name, value));
		}
	}
	
	public void unset(String name) {
		getVar(name).ifPresent(vars::remove);
	}
	
	public void unsetParam(String name) {
		getVar(name).ifPresent(vars::removeSecondary);
	}
	
	public ParseContext getContext() {
		return context;
	}
	
}
