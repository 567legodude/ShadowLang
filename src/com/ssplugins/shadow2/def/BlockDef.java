package com.ssplugins.shadow2.def;

public final class BlockDef implements MiniParser {
	
	private String name;
	private BlockCondition entryCondition;
	private BlockAction enterEvent;
	private BlockAction endEvent;
	private SectionParser parser;
	private Splitter splitter;
	
	public BlockDef(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public BlockCondition getEntryCondition() {
		return entryCondition;
	}
	
	public void setEntryCondition(BlockCondition entryCondition) {
		this.entryCondition = entryCondition;
	}
	
	public BlockAction getEnterEvent() {
		return enterEvent;
	}
	
	public void setEnterEvent(BlockAction enterEvent) {
		this.enterEvent = enterEvent;
	}
	
	public BlockAction getEndEvent() {
		return endEvent;
	}
	
	public void setEndEvent(BlockAction endEvent) {
		this.endEvent = endEvent;
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
