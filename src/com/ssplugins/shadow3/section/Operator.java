package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

import java.util.Optional;

public class Operator extends ShadowSection {
    
    private boolean leftToRight = true;
    private OpOrder order;
    
    public Operator(TokenReader reader) {
        super(reader.getLine());
        setToken(reader.expect(TokenType.OPERATOR));
    }
    
    @Override
    public Object toObject(Scope scope) {
        return getSymbol();
    }
    
    public String getSymbol() {
        return getPrimaryToken().getRaw();
    }
    
    public boolean isLeftToRight() {
        return leftToRight;
    }
    
    public void setLeftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
    }
    
    public OpOrder getOrder() {
        return order;
    }
    
    public void setOrder(OpOrder order) {
        this.order = order;
    }
    
    public enum OpOrder {
        
        UNARY,
        EXPONENT,
        MUL_DIV,
        ADD_SUB,
        COMPARE,
        EQUALITY,
        B_AND,
        B_XOR,
        B_OR,
        AND,
        OR,
        ASSIGNMENT;
    
        public static Optional<OpOrder> get(String token) {
            switch (token) {
                case "+":
                case "-":
                    return Optional.of(ADD_SUB);
                case "*":
                case "/":
                    return Optional.of(MUL_DIV);
                case "^":
                    return Optional.of(EXPONENT);
            }
            return Optional.empty();
        }
        
        public int getPrecedence() {
            return values().length - ordinal();
        }
        
    }
    
}
