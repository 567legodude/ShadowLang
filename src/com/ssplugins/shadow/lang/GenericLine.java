package com.ssplugins.shadow.lang;

class GenericLine {
	
	private int lineNumber;
	private String content;
	
	public GenericLine(int lineNumber, String content) {
		this.lineNumber = lineNumber;
		this.content = content;
	}
	
	public int lineNumber() {
		return lineNumber;
	}
	
	public String getContent() {
		return content;
	}
}
