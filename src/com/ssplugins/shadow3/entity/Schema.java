package com.ssplugins.shadow3.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Schema {
    
    public static final Schema EMPTY = new Schema(Collections.emptyList());
    
    private List<Predicate<ShadowEntity>> tests;
    
    private Schema(List<Predicate<ShadowEntity>> list) {
        tests = list;
    }
    
    public Schema() {
        this(new ArrayList<>(2));
    }
    
    public Schema(Predicate<ShadowEntity> predicate) {
        this();
        require(predicate);
    }
    
    public boolean test(ShadowEntity entity) {
        return tests.stream().allMatch(predicate -> predicate.test(entity));
    }
    
    public Schema require(Predicate<ShadowEntity> predicate) {
        tests.add(predicate);
        return this;
    }
    
    public Schema either(Predicate<ShadowEntity> a, Predicate<ShadowEntity> b) {
        return require(entity -> a.test(entity) || b.test(entity));
    }
    
    @SafeVarargs
    public final Schema or(Predicate<ShadowEntity>... tests) {
        return require(entity -> {
            for (Predicate<ShadowEntity> test : tests) {
                if (test.test(entity)) return true;
            }
            return false;
        });
    }
    
    public Schema both(Predicate<ShadowEntity> a, Predicate<ShadowEntity> b) {
        return require(entity -> a.test(entity) && b.test(entity));
    }
    
    @SafeVarargs
    public final Schema and(Predicate<ShadowEntity>... tests) {
        return require(entity -> {
            for (Predicate<ShadowEntity> test : tests) {
                if (!test.test(entity)) return false;
            }
            return true;
        });
    }
    
}
