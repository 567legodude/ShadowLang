package com.ssplugins.shadow;

import com.ssplugins.shadow.element.ShadowElement;

public interface StepperAction {
	
	void onAction(Stepper stepper, Scope scope, ShadowElement element);
	
}
