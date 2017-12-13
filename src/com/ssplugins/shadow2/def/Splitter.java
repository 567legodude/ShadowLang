package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.ParseContext;

public interface Splitter {
	
	String[] split(String content, ParseContext context);
	
	static Splitter replacerSplit() {
		return (content, context) -> new String[] {content};
	}
	
	static Splitter modSplit() {
		return (content, context) -> content.split(" +");
	}
	
}
