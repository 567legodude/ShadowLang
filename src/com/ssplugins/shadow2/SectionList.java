package com.ssplugins.shadow2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SectionList<T> {
	
	private SectionList<T> upper;
	private List<T> specific = new ArrayList<>();
	
	private Function<T, String> extractor;
	
	private SectionList(Function<T, String> extractor, SectionList<T> upper) {
		this.upper = upper;
		this.extractor = extractor;
	}
	
	public static <U> SectionList<U> create(Function<U, String> extractor) {
		return new SectionList<>(extractor, null);
	}
	
	private Predicate<T> is(String key) {
		return t -> extractor.apply(t).equals(key);
	}
	
	public SectionList<T> subsection() {
		return new SectionList<>(extractor, this);
	}
	
	public void clearSection() {
		specific.clear();
	}
	
	public boolean add(T item) {
		return specific.add(item);
	}
	
	public boolean remove(T item) {
		return specific.remove(item) || ShadowTools.get(upper).map(u -> u.remove(item)).orElse(false);
	}
	
	public boolean contains(T item) {
		return specific.contains(item) || ShadowTools.get(upper).map(u -> u.contains(item)).orElse(false);
	}
	
	public boolean hasKey(String key) {
		return specific.stream().anyMatch(is(key)) || ShadowTools.get(upper).map(u -> u.hasKey(key)).orElse(false);
	}
	
	public Optional<T> getFirst(String key) {
		Optional<T> sp = specific.stream().filter(is(key)).findFirst();
		if (sp.isPresent()) return sp;
		return ShadowTools.get(upper).flatMap(u -> u.getFirst(key));
	}
	
}
