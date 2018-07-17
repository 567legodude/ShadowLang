package com.ssplugins.shadow.def;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.element.ShadowSection;

public interface ExpressionAction {
	
	ShadowSection execute(ShadowSection left, ShadowSection right, Scope scope);

}
