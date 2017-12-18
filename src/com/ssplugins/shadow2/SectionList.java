package com.ssplugins.shadow2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SectionList<T> {
	
	private List<T> backer;
	private List<T> specific = new ArrayList<>();
	
	private Function<T, String> extractor;
	
	private SectionList(Function<T, String> extractor, List<T> backer) {
		this.backer = backer;
		this.extractor = extractor;
	}
	
	public static <U> SectionList<U> create(Function<U, String> extractor) {
		return new SectionList<>(extractor, new ArrayList<>());
	}
	
	private Predicate<T> is(String key) {
		return t -> extractor.apply(t).equals(key);
	}
	
	public SectionList<T> subsection() {
		return new SectionList<>(extractor, backer);
	}
	
	public void clearSection() {
		specific.clear();
	}
	
	public boolean add(T item) {
		return specific.add(item);
	}
	
	public boolean remove(T item) {
		return specific.remove(item) || backer.remove(item);
	}
	
	public boolean contains(T item) {
		return specific.contains(item) || backer.contains(item);
	}
	
	public boolean hasKey(String key) {
		return specific.stream().anyMatch(is(key)) || backer.stream().anyMatch(is(key));
	}
	
	public Optional<T> getFirst(String key) {
		Optional<T> sp = specific.stream().filter(is(key)).findFirst();
		if (sp.isPresent()) return sp;
		return backer.stream().filter(is(key)).findFirst();
	}
	
}
