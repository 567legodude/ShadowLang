package com.ssplugins.shadow2.common;

import java.util.Optional;

public interface ClassFinder {
	
	Optional<Class<?>> findClass(String input);
	
}
