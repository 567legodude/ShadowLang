package com.ssplugins.shadow3.parsing;

import java.util.Iterator;
import java.util.Optional;

public class TokenIterator {
    
    private Iterator<String> source;
    private int lineIndex;
    private int charIndex;
    
    private String line;
    private TokenLine tokenLine;
    private int tokenIndex;
    private StringBuilder builder = new StringBuilder();
    
    private Character c;
    private Character open = null;
    private boolean escape;
    private int sectionType;
    
    public TokenIterator(Iterator<String> source) {
        this.source = source;
    }
    
    private TokenLine tokenLine() {
        if (tokenLine == null) tokenLine = new TokenLine(line, lineIndex);
        return tokenLine;
    }
    
    public boolean hasNextLine() {
        return source.hasNext();
    }
    
    public String nextLine() {
        charIndex = 0;
        tokenLine = null;
        tokenIndex = 0;
        line = source.next();
        lineIndex++;
        return line;
    }
    
    public boolean hasNextChar() {
        return charIndex < line.length();
    }
    
    public char nextChar() {
        c = line.charAt(charIndex++);
        return c;
    }
    
    public void append() {
        builder.append(c);
        if (tokenIndex == -1) tokenIndex = charIndex;
        int len = builder.length();
        String compare = Tokenizer.COMMENT;
        if (len >= compare.length() && builder.substring(len - compare.length()).equals(compare)) {
            builder.delete(len - compare.length(), len);
            charIndex = line.length();
        }
        escape = false;
    }
    
    private void push(TokenConstructor constructor, int tokenIndex) {
        if (builder.length() == 0) return;
        tokenLine().getTokens().add(constructor.create(builder.toString(), lineIndex, tokenIndex));
        builder.setLength(0);
        this.tokenIndex = -1;
    }
    
    private void pushChar(TokenConstructor constructor) {
        pushSection();
        builder.append(c);
        push(constructor, charIndex - 1);
    }
    
    public void pushChar() {
        pushChar(Token::new);
    }
    
    public void pushSection() {
        push(Token::new, tokenIndex);
    }
    
    public void pushSection(int sectionType) {
        if (sectionType == this.sectionType) pushSection();
    }
    
    public boolean escaped() {
        return escape;
    }
    
    public void setEscape(boolean escape) {
        this.escape = escape;
    }
    
    public boolean inGroup() {
        return open != null;
    }
    
    public boolean remaining() {
        return builder.length() > 0;
    }
    
    public void closeGroup() {
        append();
        pushSection();
        open = null;
    }
    
    public String current() {
        return builder.toString();
    }
    
    public int currentSize() {
        return builder.length();
    }
    
    public boolean isEmpty() {
        return builder.length() == 0;
    }
    
    public TokenLine getTokenLine() {
        return tokenLine;
    }
    
    public int getTokenIndex() {
        return tokenIndex;
    }
    
    public Optional<TokenLine> opTokenLine() {
        return Optional.ofNullable(getTokenLine());
    }
    
    public Character getOpenChar() {
        return open;
    }
    
    public void setOpenChar() {
        this.open = c;
    }
    
    public int getSectionType() {
        return sectionType;
    }
    
    public void setSectionType(int sectionType) {
        this.sectionType = sectionType;
    }
    
    private interface TokenConstructor {
        Token create(String raw, int line, int index);
    }
    
}
