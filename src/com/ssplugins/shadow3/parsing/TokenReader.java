package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.Compound;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TokenReader extends Reader<Token> {
    
    private ShadowEntity parent;
    private ShadowParser parser;
    private TokenLine line;
    
    public TokenReader(ShadowEntity parent, ShadowParser parser, TokenLine line) {
        this.parent = parent;
        this.parser = parser;
        this.line = line;
    }
    
    @Override
    protected Token get(int index) {
        return line.getTokens().get(index);
    }
    
    @Override
    public int size() {
        return line.getTokens().size();
    }
    
    private String tokenName(TokenType type, String raw) {
        return raw == null ? type.name() : "\"" + raw + "\"";
    }
    
    public TokenType nextType() {
        return peekNext().getType();
    }
    
    public boolean nextMatches(TokenType type, String raw) {
        Token token = peekNext();
        return token.getType() == type && (raw == null || token.getRaw().equals(raw));
    }
    
    public Token expect(TokenType type) {
        return expect(type, null);
    }
    
    public Token expect(TokenType type, String raw) {
        if (!hasNext()) throw new ShadowParseError(line, line.lastToken().indexAfter(), "Expected " + type.name() + ", but reached end of line.");
        Token next = next();
        if (next.getType() != type) throw new ShadowParseError(line, next.getIndex(), "Expected " + type.name() + ", found " + next.getType().name() + ".");
        if (raw != null && !next.getRaw().equals(raw)) throw new ShadowParseError(line, next.getIndex(), "Expected \"" + raw + "\", found \"" + next.getRaw() + "\".");
        return next;
    }
    
    public <T extends ShadowSection> T expectSection(Class<T> type, String expecting) {
        Token token = peekNext();
        ShadowSection section = nextSection();
        if (!type.isInstance(section)) {
            throw new ShadowParseError(line, token.getIndex(), "Expected " + expecting + ", found " + token.getType().name());
        }
        return type.cast(section);
    }
    
    public List<ShadowSection> readTo(TokenType type) {
        return readTo(type, null);
    }
    
    public List<ShadowSection> readTo(TokenType type, String raw) {
        return readTo(token -> token.getType() == type && (raw != null && raw.equals(token.getRaw())), tokenName(type, raw), true);
    }
    
    public List<ShadowSection> readTo(Predicate<Token> predicate, String expecting, boolean include) {
        List<ShadowSection> sections = new ArrayList<>();
        boolean search = true;
        while (search && hasNext()) {
            if (predicate.test(peekNext())) search = false;
            if (!(search || include)) break; // If not searching and not included.
            sections.add(nextSection());
        }
        if (search && !hasNext()) throw new ShadowParseError(line, line.getRaw().length(), "Expecting " + expecting + ", but reached end of line.");
        return sections;
    }
    
    public Compound readCompoundValue(TokenType type) {
        return readCompoundValue(type, null);
    }
    
    public Compound readCompoundValue(TokenType type, String raw) {
        List<ShadowSection> sections = readTo(type, raw);
        sections.remove(sections.size() - 1);
        return new Compound(line, sections);
    }
    
    public ShadowSection readCompoundValue(Predicate<Token> predicate, String expecting) {
        List<ShadowSection> sections = readTo(predicate, expecting, false);
        if (sections.size() == 1) return sections.get(0);
        return new Compound(line, sections);
    }
    
    public ShadowSection readAs(TokenType type) {
        Token old = peekNext();
        TokenType oldType = old.getType();
        old.setType(type);
        ShadowSection section = nextSection();
        old.setType(oldType);
        return section;
    }
    
    public ShadowSection nextSection() {
        return parser.readSection(this);
    }
    
    public ShadowEntity getParent() {
        return parent;
    }
    
    public ShadowParser getParser() {
        return parser;
    }
    
    public TokenLine getLine() {
        return line;
    }
    
}
