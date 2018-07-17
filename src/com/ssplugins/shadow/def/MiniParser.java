package com.ssplugins.shadow.def;

public interface MiniParser {
	
	void setSectionParser(SectionParser parser);
	
	SectionParser getSectionParser();
	
	void setSplitter(Splitter splitter);
	
	Splitter getSplitter();
	
}
