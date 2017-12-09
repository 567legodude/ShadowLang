package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.element.MultiPart;
import com.ssplugins.shadow2.element.ShadowSection;

import java.util.function.BiFunction;

public final class ReplacerDef implements SectionDefinition<MultiPart> {
	
	private String token;
	private BiFunction<MultiPart, Scope, ShadowSection> action;
	
	public ReplacerDef(String token, BiFunction<MultiPart, Scope, ShadowSection> action) {
		this.token = token;
		this.action = action;
	}
	
	@Override
	public ShadowSection getValue(MultiPart content, Scope scope) {
		return action.apply(content, scope);
	}
	
	public String getToken() {
		return token;
	}
	
}
