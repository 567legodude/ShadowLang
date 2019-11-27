package com.ssplugins.shadow4.context;

import com.ssplugins.shadow4.def.OperatorType;
import com.ssplugins.shadow4.util.Precedence;

import java.util.ArrayList;
import java.util.List;

public class OperatorMap {
    
    private Precedence precedence;
    private boolean leftToRight;
    private List<OperatorType<?, ?, ?>> types;
    
    public OperatorMap(Precedence precedence, boolean leftToRight) {
        this.precedence = precedence;
        this.leftToRight = leftToRight;
        types = new ArrayList<>();
    }
    
    public Precedence getPrecedence() {
        return precedence;
    }
    
    public boolean isLeftToRight() {
        return leftToRight;
    }
    
    public List<OperatorType<?, ?, ?>> getTypes() {
        return types;
    }
    
}
