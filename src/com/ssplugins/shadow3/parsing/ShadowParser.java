package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.Shadow;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.*;
import com.ssplugins.shadow3.util.LineReader;

import java.util.List;

public class ShadowParser {
    
    public static final int ERR_NO_CLOSING = -1;
    public static final int ERR_TOO_MANY_CLOSING = -2;

    private ShadowContext context;
    
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
            if (line.endsWith(TokenType.GROUP_OPEN, "{")) ++bracket;
            else if (line.size() == 1 && line.endsWith(TokenType.GROUP_CLOSE, "}")) --bracket;
            if (bracket == 0) return i;
        }
        return ERR_NO_CLOSING;
    }
    
    public ShadowEntity readEntity(LineReader reader) {
        if (!reader.hasNext()) throw new IllegalArgumentException("Reader has no more elements.");
        if (reader.nextIsBlock()) {
            // TODO
        }
        return null;
    }
    
    public ShadowSection readSection(TokenReader reader) {
        if (!reader.hasNext()) throw ShadowException.ended(reader);
        TokenType type = reader.nextType();
        if (type == TokenType.IDENTIFIER) return new Identifier(reader);
        else if (type == TokenType.NUMBER) return new ShadowNumber(reader);
        else if (type == TokenType.STRING) return new ShadowString(reader);
        else if (type == TokenType.OPERATOR) return new Operator(reader);
        else if (type == TokenType.CALL) return new Call(reader);
        else if (type == TokenType.GROUP_OPEN) {
            String raw = reader.peekNext().getRaw();
            if (raw.equals("[")) return new InlineKeyword(reader);
            if (raw.equals("(")) return readCompound(reader, TokenType.GROUP_CLOSE, ")");
            throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Unexpected open pair.");
        }
        else if (type == TokenType.GROUP_CLOSE) {
            throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Unexpected closing bracket/parenthesis.");
        }
        throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Unexpected token.");
    }
    
    public Compound readCompound(TokenReader reader, TokenType end, String raw) {
        if (reader.nextType() == TokenType.GROUP_OPEN) {
            String close = Tokenizer.getOppositePair(reader.peekNext().getRaw());
            return readCompound(reader, TokenType.GROUP_CLOSE, close);
        }
        List<ShadowSection> sections = reader.readTo(end, raw);
        sections.remove(sections.size() - 1);
        return new Compound(reader.getLine(), sections);
    }
    
    public Shadow parse(List<String> lines, ShadowContext context) {
        List<TokenLine> tokens = new Tokenizer().tokenize(lines, context);
        LineReader reader = new LineReader(tokens, this, context);
        for (int i = 0; i < tokens.size(); ++i) {
            TokenLine line = tokens.get(i);
            if (line.endsWith(TokenType.GROUP_OPEN, "{")) {
                int end = findBlockEnd(tokens, i);
                if (end == ERR_NO_CLOSING) throw new ShadowParseError(line, line.lastToken().getIndex(), "Can't find closing bracket for block.");
                // TODO parse block
            }
            else {
                // TODO parse keyword
            }
        }
        return null;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
    public interface SectionParser {
        
        ShadowSection readSection(TokenReader reader);
        
    }
    
}
