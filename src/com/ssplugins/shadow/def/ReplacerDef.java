package com.ssplugins.shadow.def;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.element.ShadowSection;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class ReplacerDef implements MiniParser {
	
	private String token;
	private BiFunction<List<ShadowSection>, Scope, ShadowSection> action;
	
	private SectionParser parser;
	private Splitter splitter;
	
	public ReplacerDef(String token, BiFunction<List<ShadowSection>, Scope, ShadowSection> action) {
		this.token = token;
		this.action = action;
	}
	
	public static Predicate<ReplacerDef> is(String token) {
		return replacerDef -> replacerDef.getToken().equalsIgnoreCase(token);
	}
	
	public String getToken() {
		return token;
	}
	
	public BiFunction<List<ShadowSection>, Scope, ShadowSection> getAction() {
		return action;
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
