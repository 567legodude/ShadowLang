package com.ssplugins.shadow2.def;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class EvalSymbolDef implements MiniParser {
	
	public static final Pattern PATTERN = Pattern.compile("(\\W+)?(\\w+)(?:\\(([^)]+)\\))?");
	
	private String token;
	private EvalAction action;
	
	private SectionParser parser;
	
	public EvalSymbolDef(String token, EvalAction action) {
		this.token = token;
		this.action = action;
	}
	
	public static Predicate<EvalSymbolDef> is(String token) {
		return evalSymbolDef -> evalSymbolDef.getToken().equals(token);
	}
	
	public String getToken() {
		return token;
	}
	
	public EvalAction getAction() {
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
		// Unused
	}
	
	@Override
	public Splitter getSplitter() {
		return null;
	}
}
