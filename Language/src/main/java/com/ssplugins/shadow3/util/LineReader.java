package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenReader;

import java.util.List;
import java.util.function.Supplier;

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
    
    public boolean nextIsClose() {
        return peekNext().isClosing();
    }
    
    public TokenReader nextAsReader(ShadowEntity parent) {
        return new TokenReader(parent, parser, next());
    }
    
    public ShadowEntity nextEntity(Block parent, Supplier<? extends RuntimeException> failCause) {
        return parser.readEntity(parent, this, failCause);
    }
    
    public ShadowParser getParser() {
        return parser;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
}
