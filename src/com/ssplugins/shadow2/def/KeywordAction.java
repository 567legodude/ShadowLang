package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.Stepper;
import com.ssplugins.shadow2.element.ShadowSection;

import java.util.List;

public interface KeywordAction {
	
	void execute(KeywordDef def, List<ShadowSection> args, Scope scope, Stepper stepper);
	
}
