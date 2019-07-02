package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.function.Function;
import java.util.function.Predicate;

public class BasicInputModifier extends InputModifier<Object> {
    
    private Predicate<Parameters> check;
    private Function<Parameters, Object> modifier;
    
    public BasicInputModifier(TokenLine line, int index) {
        super(line, index);
        setFunction(p -> {
            if (!check.test(p)) {
                throw new ShadowCodeException(getLine(), getIndex(), "Incorrect input for this keyword.");
            }
            return modifier.apply(p);
        });
    }
    
    public void setCheck(Predicate<Parameters> check) {
        this.check = check;
    }
    
    public void setModifier(Function<Parameters, Object> modifier) {
        this.modifier = modifier;
    }
    
}
