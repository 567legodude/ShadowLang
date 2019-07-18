package com.ssplugins.shadow3.def.custom;

import java.util.function.BiFunction;

public class StringPredicate extends SimplePredicate<String, String> {
    
    public StringPredicate(String name) {
        super(name);
    }
    
    public void setTest(BiFunction<String, String, Boolean> function) {
        super.setTest(String.class, String.class, function);
    }
    
}
