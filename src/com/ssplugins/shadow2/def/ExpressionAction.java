package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.element.ShadowSection;

public interface ExpressionAction {
	
	ShadowSection execute(ShadowSection left, ShadowSection right, Scope scope);

}
