package com.ssplugins.shadow.lang;

import java.lang.reflect.Array;
import java.util.*;

class BlockIterator {
	
	private final static Map<Scope, Iterator> map = new HashMap<>();
	
	public static boolean isIterable(Object o) {
		Debugger.log("isarray: " + o.getClass().isArray());
		Debugger.log("iterable: " + Iterable.class.isAssignableFrom(o.getClass()));
		return o.getClass().isArray() || Iterable.class.isAssignableFrom(o.getClass());
	}
	
	public static boolean hasIterator(Scope scope) {
		return map.containsKey(scope);
	}
	
	public static void finish(Scope scope) {
		map.remove(scope);
	}
	
	public static Iterator getIterator(Scope scope, Object o) {
		Debugger.log("getting iterator");
		if (hasIterator(scope)) {
			Debugger.log("has iterator");
			return map.get(scope);
		}
		if (o == null) return null;
		Debugger.log("object is not null");
		Iterator it = null;
		if (o.getClass().isArray()) {
			Debugger.log("creating from array");
			it = fromArray(o);
		}
		if (Iterable.class.isAssignableFrom(o.getClass())) {
			Debugger.log("casting to iterable");
			it = ((Iterable) o).iterator();
		}
		if (it == null) return null;
		Debugger.log("returning iterator");
		map.put(scope, it);
		return it;
	}
	
	private static Iterator fromArray(Object o) {
		List<Object> list = new ArrayList<>();
		int i = 0;
		while (i != -1) {
			try {
				list.add(Array.get(o, i));
				i++;
			} catch (ArrayIndexOutOfBoundsException ignored) {
				i = -1;
			}
		}
		return list.iterator();
	}
	
}
