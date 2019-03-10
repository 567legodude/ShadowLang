package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.parsing.Tokenizer;
import com.ssplugins.shadow3.section.ShadowSection;

import java.util.List;

public class ShadowParser {
    
    public static final int ERR_NO_CLOSING = -1;
    public static final int ERR_TOO_MANY_CLOSING = -2;

    private ShadowContext context;
    private Parser parser = this::toSection;
    
    public ShadowParser() {
        context = new ShadowContext();
    }
    
    public static int findGroupEnd(Token[] tokens, int start, int end) {
        int paren = 0;
        if (end == -1) end = tokens.length;
        String ending = Tokenizer.getOppositePair(tokens[start].getRaw());
        if (ending == null) return -1;
        for (int i = start; i < end; ++i) {
            Token token = tokens[i];
            if (token.getType() == TokenType.GROUP_OPEN) ++paren;
            else if (token.getType() == TokenType.GROUP_CLOSE) --paren;
            if (paren == 0 && token.getRaw().equals(ending)) return i;
            if (paren < 0) return -2;
        }
        return -1;
    }
    
    private ShadowSection toSection(Token[] tokens) {
        return null;
    }
    
    public Shadow parse(List<String> lines) {
        List<TokenLine> tokens = new Tokenizer().tokenize(lines);
        return null;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
    public interface Parser {
        ShadowSection toSection(Token[] tokens);
    }
    
}
