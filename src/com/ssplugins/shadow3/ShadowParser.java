package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.parsing.Tokenizer;
import com.ssplugins.shadow3.section.*;

import java.util.List;
import java.util.Optional;

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
    
    public static int findBlockEnd(List<TokenLine> lines, int start) {
        int bracket = 0;
        for (int i = start; i < lines.size(); ++i) {
            TokenLine line = lines.get(i);
            Optional<Token> last = line.lastCodeToken();
            if (!last.isPresent()) continue;
            Token token = last.get();
            if (token.getType() == TokenType.GROUP_OPEN && token.getRaw().equals("{")) ++bracket;
            else if (line.size() == 1 && token.getType() == TokenType.GROUP_CLOSE && token.getRaw().equals("}")) --bracket;
            if (bracket == 0) return i;
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
        else if (Call.SCHEMA.test(tokens)) return new Call(line, tokens, parser);
        else if (InlineKeyword.SCHEMA.test(tokens)) return new InlineKeyword(line, tokens);
        throw new ShadowParseError(line, tokens[0].getIndex(), "Unable to parse section.");
    }
    
    public Shadow parse(List<String> lines) {
        List<TokenLine> tokens = new Tokenizer().tokenize(lines);
        for (int i = 0; i < tokens.size(); ++i) {
            TokenLine line = tokens.get(i);
            if (line.endsWith(TokenType.GROUP_OPEN, "{")) {
                int end = findBlockEnd(tokens, i);
                if (end == ERR_NO_CLOSING) throw new ShadowParseError(line, line.lastCodeToken().map(Token::getIndex).orElse(0), "Can't find closing bracket for block.");
                // parse block
            }
        }
        return null;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
    public interface Parser {
        ShadowSection toSection(TokenLine line, Token[] tokens);
    }
    
}
