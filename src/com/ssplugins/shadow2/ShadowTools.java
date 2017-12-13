package com.ssplugins.shadow2;

import java.util.Optional;

public final class ShadowTools {
	
	public static <T> Optional<T> get(T t) {
		return Optional.ofNullable(t);
	}
	
}
