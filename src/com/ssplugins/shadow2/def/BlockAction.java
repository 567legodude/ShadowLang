package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.Stepper;
import com.ssplugins.shadow2.element.ShadowSection;

import java.util.List;

public interface BlockAction {
	
	void trigger(BlockDef def, List<ShadowSection> mods, Scope scope, Stepper stepper);
	
}
