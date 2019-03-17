package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.parsing.Tokenizer;
import com.ssplugins.shadow3.section.*;

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
        if (ending == null) return ERR_NO_CLOSING;
        for (int i = start; i < end; ++i) {
            Token token = tokens[i];
            if (token.getType() == TokenType.GROUP_OPEN) ++paren;
            else if (token.getType() == TokenType.GROUP_CLOSE) --paren;
            if (paren == 0 && token.getRaw().equals(ending)) return i;
            if (paren < 0) return ERR_TOO_MANY_CLOSING;
        }
        return ERR_NO_CLOSING;
    }
    
    private ShadowSection toSection(TokenLine line, Token[] tokens) {
        if (tokens.length == 0) throw new IllegalArgumentException("Empty token array.");
        if (tokens.length == 1) {
            Token token = tokens[0];
            int type = token.getType();
            if (type == TokenType.STRING) return new ShadowString(line, tokens);
            else if (type == TokenType.OPERATOR) return new Operator(line, tokens);
            else if (type == TokenType.IDENTIFIER) return new Identifier(line, tokens);
            else if (type == TokenType.NUMBER) return new ShadowNumber(line, tokens);
        }
        else {
            //
        }
        throw new ShadowParseError(line, tokens[0].getIndex(), "Unable to parse section.");
    }
    
    public Shadow parse(List<String> lines) {
        List<TokenLine> tokens = new Tokenizer().tokenize(lines);
        return null;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
    public interface Parser {
        ShadowSection toSection(TokenLine line, Token[] tokens);
    }
    
}
