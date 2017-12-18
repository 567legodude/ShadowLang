package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.ShadowElement;

public interface StepperAction {
	
	void onAction(Stepper stepper, Scope scope, ShadowElement element);
	
}
