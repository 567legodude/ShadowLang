package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.common.TypeReference;
import com.ssplugins.shadow2.element.EvalSection;

public interface EvalAction {
	
	TypeReference execute(TypeReference reference, EvalSection section);
	
}
