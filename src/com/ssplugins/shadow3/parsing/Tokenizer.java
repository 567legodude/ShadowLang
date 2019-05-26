package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.exception.ShadowParseError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Tokenizer {
    
    public static final char ESCAPE_CHAR = '\\';
    public static final String GROUPS = "\"'";
    public static final String PAIRS = "(){}[]<>";
    public static final String COMMENT = "//";
    
    public static final int TYPE_STD = 0;
    public static final int TYPE_OTHER = 1;
    
    public static String getOppositePair(String pair) {
        int i = PAIRS.indexOf(pair);
        if (i == -1) return null;
        if (evenIndex(i)) return String.valueOf(PAIRS.charAt(i + 1));
        else return String.valueOf(PAIRS.charAt(i - 1));
    }
    
    public List<TokenLine> tokenize(List<String> lines, ShadowContext context) {
        List<TokenLine> output = new ArrayList<>(lines.size());
        TokenIterator it = new TokenIterator(lines);
        Set<String> allOps = context.operators();
        Set<String> ops = new HashSet<>(allOps);
        while (it.hasNextLine()) {
            String line = it.nextLine();
            while (it.hasNextChar()) {
                char c = it.nextChar();
                if (it.escaped()) it.append();
                else if (c == ESCAPE_CHAR) it.setEscape(true);
                else if (it.inGroup()) {
                    if (c == it.getOpenChar()) it.closeGroup();
                    else it.append();
                }
                else if (GROUPS.indexOf(c) > -1) {
                    it.pushSection();
                    it.append();
                    it.setOpenChar();
                }
                else if (Character.isWhitespace(c)) it.pushSection();
                else if (PAIRS.indexOf(c) > -1) {
                    it.pushChar();
                }
                else if (!Character.isLetterOrDigit(c) && c != '_') {
                    it.pushSection(TYPE_STD);
                    it.setSectionType(TYPE_OTHER);
                    // Split operators at the longest matching token
                    int index = it.currentSize();
                    ops.removeIf(s -> s.length() <= index || s.charAt(index) != c);
                    if (ops.size() == 0) {
                        it.pushSection();
                        for (String s : allOps) {
                            if (s.charAt(0) == c) ops.add(s);
                        }
                    }
                    it.append();
                    if (ops.size() == 1 && index + 1 == ops.iterator().next().length()) {
                        it.pushSection();
                        ops.addAll(allOps);
                    }
                }
                else {
                    it.pushSection(TYPE_OTHER);
                    it.setSectionType(TYPE_STD);
                    it.append();
                }
            }
            if (it.inGroup()) throw new ShadowParseError(it.getTokenLine(), line.lastIndexOf(it.getOpenChar()), "Unclosed quote.");
            if (it.remaining()) it.pushSection();
            it.opTokenLine().map(tokenLine -> setTypes(tokenLine, context)).ifPresent(output::add);
        }
        return output;
    }
    
    private TokenLine setTypes(TokenLine line, ShadowContext context) {
        Token last = null;
        int index;
        List<Token> tokens = line.getTokens();
        for (int i = 0; i < tokens.size(); ++i) {
            Token token = tokens.get(i);
            char first = token.getRaw().charAt(0);
            TokenType type;
            if (Character.isLetter(first) || first == '_') type = TokenType.IDENTIFIER;
            else if (Character.isDigit(first)) {
                type = TokenType.NUMBER;
                int offset = 0;
                offset += tryCombine(tokens, i + 1, token, s -> s.equals("."));
                if (offset > 0) {
                    offset += tryCombine(tokens, i + 2, token, s -> Character.isDigit(s.charAt(0)));
                }
                while (offset > 0) {
                    tokens.remove(i + 1);
                    --offset;
                }
            }
            else if ((index = PAIRS.indexOf(first)) > -1) {
                if (evenIndex(index)) {
                    type = TokenType.GROUP_OPEN;
                    if (first == '(' && last != null && last.getType() == TokenType.IDENTIFIER) {
                        last.setType(TokenType.CALL);
                    }
                    else if (i == tokens.size() - 1 && token.getRaw().equals("{")) line.setBlock(true, i);
                }
                else {
                    type = TokenType.GROUP_CLOSE;
                }
            }
            else if (GROUPS.indexOf(first) > -1) type = TokenType.STRING;
            else {
                type = TokenType.OPERATOR;
                if (token.getRaw().equals("::")) line.setBlock(true, i);
            }
            token.setType(type);
            last = token;
        }
        return line;
    }
    
    private int tryCombine(List<Token> tokens, int index, Token token, Predicate<String> predicate) {
        if (index >= tokens.size()) return 0;
        Token next = tokens.get(index);
        if (predicate.test(next.getRaw())) {
            token.append(next);
            return 1;
        }
        return 0;
    }
    
    private static boolean evenIndex(int n) {
        return (n & 1) == 0; // or mod 2, whatever works
    }
    
}
