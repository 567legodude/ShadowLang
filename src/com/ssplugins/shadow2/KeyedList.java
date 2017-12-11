package com.ssplugins.shadow2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KeyedList<T> extends ArrayList<T> {
	
	private Function<T, String> extractor;
	
	public KeyedList(Function<T, String> extractor) {
		this.extractor = extractor;
	}
	
	public T getFirst(String key) {
		for (T t : this) {
			if (extractor.apply(t).equals(key)) return t;
		}
		return null;
	}
	
	public boolean hasKey(String key) {
		return this.stream().anyMatch(t -> extractor.apply(t).equals(key));
	}
	
	public boolean hasKey(T item) {
		return hasKey(extractor.apply(item));
	}
	
	public boolean removeKey(String key) {
		return this.removeIf(t -> extractor.apply(t).equals(key));
	}
	
	public boolean removeKey(String key, Predicate<T> predicate) {
		return this.removeIf(t -> extractor.apply(t).equals(key) && predicate.test(t));
	}
	
	public void forEachKey(String key, Consumer<T> consumer) {
		this.stream().filter(t -> extractor.apply(t).equals(key)).forEach(consumer);
	}
	
	public List<T> filter(Predicate<T> predicate) {
		return this.stream().filter(predicate).collect(Collectors.toList());
	}
	
	public List<String> duplicateKeys() {
		List<String> checked = new ArrayList<>();
		List<String> dupes = new ArrayList<>();
		this.forEach(t -> {
			String key = extractor.apply(t);
			if (checked.contains(key)) {
				if (!dupes.contains(key)) dupes.add(key);
			}
			else checked.add(key);
		});
		return dupes;
	}
	
}
