package com.ssplugins.shadow.def;

import com.ssplugins.shadow.common.Range;

import java.util.function.Predicate;

public final class BlockDef implements MiniParser {
	
	private String name;
	private BlockCondition entryCondition;
	private BlockAction enterEvent;
	private BlockAction endEvent;
	private Range modifierCount = Range.any();
	private Range parameterCount = Range.any();
	private boolean provideParams;
	
	private SectionParser parser;
	private Splitter splitter;
	
	public BlockDef(String name) {
		this.name = name;
	}
	
	public static BlockDef temporary(String name) {
		return new BlockDef(name);
	}
	
	public static Predicate<BlockDef> is(String name) {
		return blockDef -> blockDef.getName().equalsIgnoreCase(name);
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
	
	public Range getModifierCount() {
		return modifierCount;
	}
	
	public void setModifierCount(Range modifierCount) {
		if (modifierCount == null) modifierCount = Range.any();
		this.modifierCount = modifierCount;
	}
	
	public Range getParameterCount() {
		return parameterCount;
	}
	
	public void setParameterCount(Range parameterCount) {
		if (parameterCount == null) parameterCount = Range.any();
		this.parameterCount = parameterCount;
	}
	
	public boolean paramsProvided() {
		return provideParams;
	}
	
	public void setProvideParams(boolean provideParams) {
		this.provideParams = provideParams;
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