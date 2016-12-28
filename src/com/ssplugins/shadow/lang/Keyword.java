package com.ssplugins.shadow.lang;

public class Keyword {
	
	private String word;
	private KeywordAction action = null;
	
	public Keyword(String word) {
		this.word = word;
	}
	
	public Keyword(String word, KeywordAction action) {
		this.word = word;
		this.action = action;
	}
	
	void rename(String newName) {
		word = newName;
	}
	
	public String getKeyword() {
		return word;
	}
	
	public void setAction(KeywordAction action) {
		this.action = action;
	}
	
	void execute(String[] args, Scope scope, Stepper stepper) {
		if (action != null) action.used(args, scope, stepper);
	}
	
}
