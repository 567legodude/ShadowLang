package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.Shadow;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.EntityList;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.*;
import com.ssplugins.shadow3.util.LineReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ShadowParser {
    
    public static final int ERR_NO_CLOSING = -1;
    public static final int ERR_TOO_MANY_CLOSING = -2;

    private ShadowContext context;
    
    public ShadowParser(ShadowContext context) {
        this.context = context;
    }
    
    public static int findGroupEnd(List<Token> tokens, int start, int end) {
        int paren = 0;
        if (end == -1) end = tokens.size();
        String ending = Tokenizer.getOppositePair(tokens.get(start).getRaw());
        if (ending == null) return ERR_NO_CLOSING;
        for (int i = start; i < end; ++i) {
            Token token = tokens.get(i);
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
    
    public ShadowEntity readEntity(Block parent, LineReader reader, Supplier<? extends RuntimeException> failCause) {
        if (!reader.hasNext()) throw failCause.get();
        if (reader.nextIsBlock()) return readBlock(parent, reader);
        else return readKeyword(parent, reader.nextAsReader(parent), reader.getContext());
    }
    
    public Block readBlock(Block parent, LineReader reader) {
        return new Block(parent, reader);
    }
    
    public Keyword readKeyword(ShadowEntity parent, TokenReader reader, ShadowContext fallback) {
        return new Keyword(parent, reader, fallback);
    }
    
    public ShadowSection readSection(TokenReader reader) {
        if (!reader.hasNext()) throw ShadowCodeException.ended(reader);
        TokenType type = reader.nextType();
        if (type == TokenType.IDENTIFIER) return new Identifier(reader);
        else if (type == TokenType.NUMBER) return new ShadowNumber(reader);
        else if (type == TokenType.STRING) return new ShadowString(reader);
        else if (type == TokenType.BOOLEAN) return new ShadowBoolean(reader);
        else if (type == TokenType.OPERATOR) return new Operator(reader);
        else if (type == TokenType.GROUP_OPEN) {
            String raw = reader.peekNext().getRaw();
            if (raw.equals("[")) return new InlineKeyword(reader.getParent(), reader, this);
            if (raw.equals("(")) return readCompound(reader, TokenType.GROUP_CLOSE, ")");
            throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Unexpected open pair.");
        }
        else if (type == TokenType.GROUP_CLOSE) return new Dummy(reader);
        throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Unexpected token.");
    }
    
    public Compound readCompound(TokenReader reader, TokenType end, String raw) {
//        if (reader.nextType() == TokenType.GROUP_OPEN) {
//            String close = Tokenizer.getOppositePair(reader.peekNext().getRaw());
//            reader.consume();
//            return readCompound(reader, TokenType.GROUP_CLOSE, close);
//        }
        reader.consume();
        List<ShadowSection> sections = reader.readTo(end, raw);
        sections.remove(sections.size() - 1);
        return new Compound(reader.getLine(), sections, reader.getParent());
    }
    
    public Shadow parse(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return parse(lines.iterator());
        } catch (IOException e) {
            throw new ShadowException(e);
        }
    }
    
    public Shadow parse(Iterator<String> lines) {
        List<TokenLine> tokens = new Tokenizer().tokenize(lines, context);
        Shadow shadow = new Shadow(context);
        EntityList contents = shadow.getContents();
        LineReader reader = new LineReader(tokens, this, context);
        while (reader.hasNext()) {
            contents.add(reader.nextEntity(null, null));
        }
        return shadow;
    }
    
    public ShadowContext getContext() {
        return context;
    }
    
}
