package com.ssplugins.shadow3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Schema<T> implements Predicate<T> {
    
    public static final Schema<Object> EMPTY = new Schema<>(Collections.emptyList());
    
    private List<Predicate<T>> tests;
    private String situation = "";
    
    private Schema(List<Predicate<T>> list) {
        tests = list;
    }
    
    public Schema() {
        this(new ArrayList<>(2));
    }
    
    public Schema(Predicate<T> predicate) {
        this();
        require(predicate);
    }
    
    public static <U> Schema<U> single(Predicate<U> predicate) {
        return new Schema<>(Collections.singletonList(predicate));
    }
    
    @Override
    public boolean test(T entity) {
        return tests.stream().allMatch(predicate -> predicate.test(entity));
    }
    
    public String getSituation() {
        return situation;
    }
    
    public void setSituation(String situation) {
        this.situation = situation;
    }
    
    public Schema<T> require(Predicate<T> predicate) {
        tests.add(predicate);
        return this;
    }
    
    public Schema<T> either(Predicate<T> a, Predicate<T> b) {
        return require(entity -> a.test(entity) || b.test(entity));
    }
    
    @SafeVarargs
    public final Schema<T> or(Predicate<T>... tests) {
        return require(entity -> {
            for (Predicate<T> test : tests) {
                if (test.test(entity)) return true;
            }
            return false;
        });
    }
    
    public Schema<T> both(Predicate<T> a, Predicate<T> b) {
        return require(entity -> a.test(entity) && b.test(entity));
    }
    
    @SafeVarargs
    public final Schema<T> and(Predicate<T>... tests) {
        return require(entity -> {
            for (Predicate<T> test : tests) {
                if (!test.test(entity)) return false;
            }
            return true;
        });
    }
    
}
