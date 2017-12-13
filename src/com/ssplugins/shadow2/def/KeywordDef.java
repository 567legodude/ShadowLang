package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.common.Range;

import java.util.function.Predicate;

public final class KeywordDef implements MiniParser {
	
	private String keyword;
	private KeywordAction action;
	private Range argumentCount = Range.any();
	
	private SectionParser parser;
	private Splitter splitter;
	
	public KeywordDef(String keyword, KeywordAction action) {
		this.keyword = keyword;
		this.action = action;
	}
	
	public static Predicate<KeywordDef> is(String keyword) {
		return keywordDef -> keywordDef.getKeyword().equalsIgnoreCase(keyword);
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public KeywordAction getAction() {
		return action;
	}
	
	public Range getArgumentCount() {
		return argumentCount;
	}
	
	public void setArgumentCount(Range argumentCount) {
		if (argumentCount == null) argumentCount = Range.any();
		this.argumentCount = argumentCount;
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
