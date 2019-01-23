package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.exception.NamedShadowException;
import com.ssplugins.shadow3.parsing.token.CloseGroup;
import com.ssplugins.shadow3.parsing.token.OpenGroup;
import com.ssplugins.shadow3.parsing.token.Section;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    
    public static final char ESCAPE_CHAR = '\\';
    public static final String GROUPS = "\"'";
    public static final String PAIRS = "(){}[]";
    public static final String COMMENT = "//";
    
    public List<TokenLine> tokenize(List<String> lines) {
        List<TokenLine> output = new ArrayList<>(lines.size());
        StringBuilder token = new StringBuilder();
        TokenLine tokenLine;
        TokenIndex index = new TokenIndex();
        char open = '\0';
        boolean escape = false;
        for (int lineIndex = 0, size = lines.size(); lineIndex < size; ++lineIndex) {
            token.setLength(0);
            String line = lines.get(lineIndex);
            if (line.isEmpty()) continue;
            tokenLine = new TokenLine(line, lineIndex + 1);
            for (int charIndex = 0, len = line.length(); charIndex < len; ++charIndex) {
                char c = line.charAt(charIndex);
                String cs = String.valueOf(c);
                if (escape) { // Escape characters
                    escape = false;
                    if (append(token, index, charIndex, c)) break;
                    continue;
                }
                if (c == ESCAPE_CHAR) {
                    escape = true;
                    continue;
                }
                if (open != '\0') { // Add all characters between quotes
                    if (append(token, index, charIndex, c)) break;
                    if (c == open) {
                        pushSection(token, tokenLine, lineIndex, index);
                        open = '\0';
                    }
                    continue;
                }
                if (GROUPS.contains(cs)) { // Find quote start
                    open = c;
                    if (append(token, index, charIndex, c)) break;
                    continue;
                }
                if (Character.isWhitespace(c)) { // Split on any other whitespace
                    pushSection(token, tokenLine, lineIndex, index);
                    continue;
                }
                int ind = PAIRS.indexOf(c);
                if (ind > -1) { // Brackets are delimiters.
                    boolean op = evenIndex(ind);
                    if (op) pushOpen(token, cs, tokenLine, lineIndex, charIndex, index);
                    else pushClose(token, cs, tokenLine, lineIndex, charIndex, index);
                    continue;
                }
                if (append(token, index, charIndex, c)) break;
            }
            if (tokenLine.getTokens().size() == 0) continue;
            if (token.length() > 0) {
                pushSection(token, tokenLine, lineIndex, index);
            }
            output.add(tokenLine);
        }
        return output;
    }
    
    public List<TokenLine> tokenize2(List<String> lines) {
        List<TokenLine> output = new ArrayList<>(lines.size());
        TokenIterator it = new TokenIterator(lines);
        int index;
        while (it.hasNextLine()) {
            it.nextLine();
            while (it.hasNextChar()) {
                char c = it.nextChar();
                if (it.escaped()) it.append();
                else if (c == ESCAPE_CHAR) it.setEscape(true);
                else if (it.inGroup() && c == it.getOpenChar()) {
                    it.append();
                    it.pushSection();
                }
                else if (GROUPS.contains(String.valueOf(c))) {
                    it.append();
                    it.setOpenChar();
                }
                else if (Character.isWhitespace(c)) it.pushSection();
                else if ((index = PAIRS.indexOf(c)) > -1) {
                    if (evenIndex(index)) it.pushOpen();
                    else it.pushClose();
                }
                else it.append();
            }
            if (it.inGroup()) throw new NamedShadowException("ParseError", it.getTokenLine(), -1, "Unclosed quote.");
            if (it.remaining()) it.pushSection();
            it.opTokenLine().ifPresent(output::add);
        }
        return output;
    }
    
    // Add character, mark index, and stop reading if we reached a comment.
    private boolean append(StringBuilder builder, TokenIndex index, int charIndex, char c) {
        builder.append(c);
        index.mark(charIndex);
        int ind = builder.indexOf(COMMENT);
        if (ind > -1) {
            builder.delete(ind, builder.length());
            return true;
        }
        return false;
    }
    
    private void pushSection(StringBuilder builder, TokenLine tokenLine, int lineIndex, TokenIndex sectionIndex) {
        if (builder.length() == 0) return;
        tokenLine.getTokens().add(new Section(builder.toString(), lineIndex + 1, sectionIndex.get()));
        builder.setLength(0);
        sectionIndex.reset();
    }
    
    private void pushOpen(StringBuilder builder, String bracket, TokenLine tokenLine, int lineIndex, int index, TokenIndex sectionIndex) {
        pushSection(builder, tokenLine, lineIndex, sectionIndex);
        tokenLine.getTokens().add(new OpenGroup(bracket, lineIndex + 1, index));
    }
    
    private void pushClose(StringBuilder builder, String bracket, TokenLine tokenLine, int lineIndex, int index, TokenIndex sectionIndex) {
        pushSection(builder, tokenLine, lineIndex, sectionIndex);
        tokenLine.getTokens().add(new CloseGroup(bracket, lineIndex + 1, index));
    }
    
    private boolean evenIndex(int n) {
        return (n & 1) == 0;
    }
    
}
