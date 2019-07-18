package com.ssplugins.shadow3.parsing;

import java.util.List;

public class SubTokenReader extends TokenReader {
    
    private int start;
    private int end;
    
    public SubTokenReader(TokenReader reader, int start, int end) {
        super(reader.getParent(), reader.getParser(), reader.getLine());
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Token get(int index) {
        return getLine().getTokens().get(index + start);
    }
    
    @Override
    public int size() {
        return end - start;
    }
    
    @Override
    public List<Token> getTokens() {
        return super.getTokens().subList(start, end);
    }
    
    @Override
    public TokenReader subReader(int start, int end) {
        return super.subReader(this.start + start, this.start + end);
    }
    
}
