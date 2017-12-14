package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.ShadowSection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ShadowTools {
	
	public static <T> Optional<T> get(T t) {
		return Optional.ofNullable(t);
	}
	
	public static <T> String asString(List<T> list) {
		return asString(list, 0, list.size());
	}
	
	public static <T> String asString(List<T> list, int start, int end) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			builder.append(list.get(i));
		}
		return builder.toString();
	}
	
	public static <T> List<T> lockList(List<T> list) {
		return Collections.unmodifiableList(list);
	}
	
}
