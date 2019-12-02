package com.ssplugins.shadow4.parsing;

import com.ssplugins.shadow4.Shadow;
import com.ssplugins.shadow4.context.Context;
import com.ssplugins.shadow4.tokens.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ShadowParser {
    
    private Context context;
    
    public ShadowParser(Context context) {
        this.context = context;
    }
    
    private boolean matchesBlock(List<Token> tokens) {
        if (tokens.size() > 1) {
            if (tokens.get(tokens.size() - 1).matches(TokenType.GROUP_OPEN, "{"))
        }
    }
    
    public Shadow parse(Iterator<String> source) {
        List<Line> lines = new LinkedList<>();
        Tokenizer tokenizer = new Tokenizer(context);
        TokenBuilder tokenBuilder = new TokenBuilder();
        int line = 1;
        while (source.hasNext()) {
            List<Token> tokens = tokenizer.tokenize(source.next(), line++, tokenBuilder);
            
        }
    }
    
}
