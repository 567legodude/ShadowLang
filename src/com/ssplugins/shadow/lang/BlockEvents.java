package com.ssplugins.shadow.lang;

public class BlockEvents {
	
	private BlockPreRunEvent preRunEvent;
	private BlockEnterEvent enterEvent;
	private BlockEndEvent endEvent;
	
	public BlockEvents() {}
	
	public BlockPreRunEvent getPreRunEvent() {
		return preRunEvent;
	}
	
	public void setPreRunEvent(BlockPreRunEvent preRunEvent) {
		this.preRunEvent = preRunEvent;
	}
	
	public BlockEnterEvent getEnterEvent() {
		return enterEvent;
	}
	
	public void setEnterEvent(BlockEnterEvent enterEvent) {
		this.enterEvent = enterEvent;
	}
	
	public BlockEndEvent getEndEvent() {
		return endEvent;
	}
	
	public void setEndEvent(BlockEndEvent endEvent) {
		this.endEvent = endEvent;
	}
}
