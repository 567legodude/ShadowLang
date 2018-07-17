package com.ssplugins.shadow.common;

import java.util.Optional;

public interface ClassFinder {
	
	Optional<Class<?>> findClass(String input);
	
}
