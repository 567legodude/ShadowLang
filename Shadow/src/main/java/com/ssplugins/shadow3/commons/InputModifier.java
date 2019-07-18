package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.function.Function;

public abstract class InputModifier<T> {
    
    private TokenLine line;
    private int index;
    private Function<Parameters, T> function;
    
    public InputModifier(TokenLine line, int index) {
        this.line = line;
        this.index = index;
    }
    
    protected void setFunction(Function<Parameters, T> modifier) {
        this.function = modifier;
    }
    
    public TokenLine getLine() {
        return line;
    }
    
    public int getIndex() {
        return index;
    }
    
    public Function<Parameters, T> getFunction() {
        return function;
    }
    
}
