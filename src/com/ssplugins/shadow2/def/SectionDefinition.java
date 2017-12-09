package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.element.ShadowSection;

public interface SectionDefinition<T extends ShadowSection> {
	
	ShadowSection getValue(T content, Scope scope);
	
}
