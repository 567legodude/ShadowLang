package com.ssplugins.shadow.def;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.Stepper;
import com.ssplugins.shadow.element.ShadowSection;

import java.util.List;

public interface BlockAction {
	
	void trigger(BlockDef def, List<ShadowSection> mods, List<String> parameters, Scope scope, Stepper stepper);
	
}
