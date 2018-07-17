package com.ssplugins.shadow.def;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.common.TypeReference;
import com.ssplugins.shadow.element.EvalSection;

public interface EvalAction {
	
	TypeReference execute(TypeReference reference, EvalSection section, Scope scope);
	
}
