package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.element.MultiPart;
import com.ssplugins.shadow2.element.ShadowSection;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class ReplacerDef implements SectionDefinition<MultiPart>, MiniParser {
	
	private String token;
	private BiFunction<MultiPart, Scope, ShadowSection> action;
	
	private SectionParser parser;
	private Splitter splitter;
	
	public ReplacerDef(String token, BiFunction<MultiPart, Scope, ShadowSection> action) {
		this.token = token;
		this.action = action;
	}
	
	public static Predicate<ReplacerDef> is(String token) {
		return replacerDef -> replacerDef.getToken().equalsIgnoreCase(token);
	}
	
	@Override
	public ShadowSection getValue(MultiPart content, Scope scope) {
		return action.apply(content, scope);
	}
	
	public String getToken() {
		return token;
	}
	
	@Override
	public void setSectionParser(SectionParser parser) {
		this.parser = parser;
	}
	
	@Override
	public SectionParser getSectionParser() {
		return parser;
	}
	
	@Override
	public void setSplitter(Splitter splitter) {
		this.splitter = splitter;
	}
	
	@Override
	public Splitter getSplitter() {
		return splitter;
	}
	
}
