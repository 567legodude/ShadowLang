package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.ParseContext;

import java.util.List;

public interface Splitter {
	
	List<String> split(String content, ParseContext context);
	
}
