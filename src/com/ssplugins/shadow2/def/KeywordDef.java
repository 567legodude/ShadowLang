package com.ssplugins.shadow2.def;

public final class KeywordDef implements MiniParser {
	
	private String keyword;
	private KeywordAction action;
	
	private SectionParser parser;
	private Splitter splitter;
	
	public KeywordDef(String keyword, KeywordAction action) {
		this.keyword = keyword;
		this.action = action;
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public KeywordAction getAction() {
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
