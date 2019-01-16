package com.ssplugins.shadow.common;

import com.ssplugins.shadow.Scope;

import java.lang.reflect.Array;
import java.util.*;

public class ShadowIterator {
	
	private final static Map<Scope, Iterator> LOOPS = new HashMap<>();
	
	public static boolean isIterable(Object o) {
		Class<?> type = o.getClass();
		return type.isArray() || Iterable.class.isAssignableFrom(type) || Iterator.class.isAssignableFrom(type);
	}
	
	public static boolean hasIterator(Scope scope) {
		return LOOPS.containsKey(scope);
	}
	
	public static void endLoop(Scope scope) {
		LOOPS.remove(scope);
	}
	
	public static Optional<Iterator> getIterator(Scope scope, Object o) {
		if (hasIterator(scope)) {
			return Optional.of(LOOPS.get(scope));
		}
		if (o == null) return Optional.empty();
		Class<?> type = o.getClass();
		Iterator it = null;
		if (type.isArray()) {
			it = fromArray(o);
		}
		else if (Iterable.class.isAssignableFrom(type)) {
			it = ((Iterable) o).iterator();
		}
		else if (Iterator.class.isAssignableFrom(type)) {
			it = (Iterator) o;
		}
		if (it == null) return Optional.empty();
		LOOPS.put(scope, it);
		return Optional.of(it);
	}
	
	private static Iterator fromArray(Object o) {
        int len = Array.getLength(o);
		List<Object> list = new ArrayList<>(len);
        for (int i = 0; i < len; ++i) {
            list.add(Array.get(o, i));
        }
		return list.iterator();
	}

}
