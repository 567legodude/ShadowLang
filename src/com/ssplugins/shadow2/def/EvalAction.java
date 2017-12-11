package com.ssplugins.shadow2.def;

import java.util.concurrent.atomic.AtomicReference;

public interface EvalAction {
	
	AtomicReference<Object> execute(AtomicReference<Object> current);
	
}
