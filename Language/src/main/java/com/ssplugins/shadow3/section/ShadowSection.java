package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;

public abstract class ShadowSection {
    
    private TokenLine line;
    private Token[] tokens;
    
    public ShadowSection(TokenLine line) {
        this.line = line;
    }
    
    public abstract Object toObject(Scope scope);
    
    protected void setTokens(Token[] tokens) {
        this.tokens = tokens;
    }
    
    protected void setToken(Token token) {
        setTokens(new Token[] {token});
    }
    
    public Token getPrimaryToken() {
        return tokens[0];
    }
    
    public TokenLine getLine() {
        return line;
    }
    
    public Token[] getTokens() {
        return tokens;
    }
    
    public String stringValue(Scope scope) {
        return toObject(scope).toString();
    }
    
    public <T> T getValue(Class<T> type, Scope scope, String msg) {
        Object o = this.toObject(scope);
        if (!type.isInstance(o)) {
            throw new ShadowExecutionError(line, tokens[0].getIndex(), msg);
        }
        return type.cast(o);
    }
    
}
