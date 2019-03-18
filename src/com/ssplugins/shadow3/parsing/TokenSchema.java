package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.entity.Schema;

public class TokenSchema extends Schema<Token[]> {
    
    private int index;
    
    public TokenSchema minLength(int length) {
        require(tokens -> tokens.length >= length);
        return this;
    }
    
    public TokenSchema type(int type) {
        int i = index++;
        require(tokens -> tokens[i].getType() == type);
        return this;
    }
    
    public TokenSchema type(int type, String raw) {
        int i = index++;
        require(tokens -> tokens[i].getType() == type && tokens[i].getRaw().equals(raw));
        return this;
    }
    
    public TokenSchema typeLast(int type) {
        require(tokens -> tokens[tokens.length - 1].getType() == type);
        return this;
    }
    
    public TokenSchema typeLast(int type, String raw) {
        require(tokens -> {
            int i = tokens.length - 1;
            return tokens[i].getType() == type && tokens[i].getRaw().equals(raw);
        });
        return this;
    }
    
}
