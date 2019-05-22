package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.parsing.TokenLine;

import java.util.List;

public class LineReader extends ListReader<TokenLine> {
    
    private ShadowParser parser;
    private ShadowContext context;
    
    public LineReader(List<TokenLine> list, ShadowParser parser, ShadowContext context) {
        super(list);
        this.parser = parser;
        this.context = context;
    }
    
    public boolean nextIsBlock() {
        return peekNext().isBlock();
    }
    
    public ShadowParser getParser() {
        return parser;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
}
