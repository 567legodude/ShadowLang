package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.ParseContext;
import com.ssplugins.shadow2.element.ShadowSection;

import java.util.List;

public interface SectionParser {
	
	List<ShadowSection> getSections(List<String> sections, ParseContext context);
	
}
