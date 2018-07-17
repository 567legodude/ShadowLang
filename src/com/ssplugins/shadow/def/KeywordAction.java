package com.ssplugins.shadow.def;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.Stepper;
import com.ssplugins.shadow.element.ShadowSection;

import java.util.List;

public interface KeywordAction {
	
	void execute(KeywordDef def, List<ShadowSection> args, Scope scope, Stepper stepper);
	
}
