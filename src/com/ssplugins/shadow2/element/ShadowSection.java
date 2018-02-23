package com.ssplugins.shadow2.element;

public abstract class ShadowSection {
	
	public boolean isPrimitive() {
		return this instanceof Primitive;
	}
	
	public boolean isReplacer() {
		return this instanceof Replacer;
	}
	
	public boolean isMultiPart() {
		return this instanceof MultiPart;
	}
	
	public boolean isEvalGroup() {
		return this instanceof EvalGroup;
	}
	
	public boolean isLazyReplacer() {
		return this instanceof LazyReplacers;
	}
	
	public boolean isReference() {
		return this instanceof Reference;
	}
	
	public boolean isExpression() {
		return this instanceof Expression;
	}
	
	public boolean isScopeVar() {
		return this instanceof ScopeVar;
	}
	
	public boolean isEmpty() {
		return this instanceof Empty;
	}
	
	public Primitive asPrimitive() {
		if (isPrimitive()) {
			return (Primitive) this;
		}
		throw new IllegalStateException("Object is not Primitive.");
	}
	
	public Replacer asReplacer() {
		if (isReplacer()) {
			return (Replacer) this;
		}
		throw new IllegalStateException("Object is not Replacer.");
	}
	
	public MultiPart asMultiPart() {
		if (isMultiPart()) {
			return (MultiPart) this;
		}
		throw new IllegalStateException("Object is not MultiPart.");
	}
	
	public EvalGroup asEvalGroup() {
		if (isEvalGroup()) {
			return (EvalGroup) this;
		}
		throw new IllegalStateException("Object is not EvalGroup.");
	}
	
	public LazyReplacers asLazyReplacer() {
		if (isLazyReplacer()) {
			return (LazyReplacers) this;
		}
		throw new IllegalStateException("Object is not LazyReplacers.");
	}
	
	public Reference asReference() {
		if (isReference()) {
			return (Reference) this;
		}
		throw new IllegalStateException("Object is not Reference.");
	}
	
	public Expression asExpression() {
		if (isExpression()) {
			return (Expression) this;
		}
		throw new IllegalStateException("Object is not Expression.");
	}
	
	public ScopeVar asScopeVar() {
		if (isScopeVar()) {
			return (ScopeVar) this;
		}
		throw new IllegalStateException("Object is not ScopeVar.");
	}

}
