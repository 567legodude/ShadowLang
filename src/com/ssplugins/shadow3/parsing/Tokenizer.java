package com.ssplugins.shadow3.parsing;

import com.ssplugins.shadow3.exception.NamedShadowException;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    
    public static final char ESCAPE_CHAR = '\\';
    public static final String GROUPS = "\"'";
    public static final String PAIRS = "(){}[]";
    public static final String COMMENT = "//";
    
    public static final int TYPE_STD = 0;
    public static final int TYPE_OTHER = 1;
    
    public static String getOppositePair(String pair) {
        int i = PAIRS.indexOf(pair);
        if (i == -1) return null;
        if (evenIndex(i)) return String.valueOf(PAIRS.charAt(i + 1));
        else return String.valueOf(PAIRS.charAt(i - 1));
    }
    
    public List<TokenLine> tokenize(List<String> lines) {
        List<TokenLine> output = new ArrayList<>(lines.size());
        TokenIterator it = new TokenIterator(lines);
//        int index;
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
//                else if ((index = PAIRS.indexOf(c)) > -1) {
//                    if (evenIndex(index)) it.pushOpen();
//                    else it.pushClose();
//                }
                else if (!Character.isLetterOrDigit(c) && c != '_') {
                    it.pushSection(TYPE_STD);
                    it.setSectionType(TYPE_OTHER);
                    it.append();
                }
                else {
                    it.pushSection(TYPE_OTHER);
                    it.setSectionType(TYPE_STD);
                    it.append();
                }
            }
            if (it.inGroup()) throw new NamedShadowException("ParseError", it.getTokenLine(), line.lastIndexOf(it.getOpenChar()), "Unclosed quote.");
            if (it.remaining()) it.pushSection();
            it.opTokenLine().map(this::setTypes).ifPresent(output::add);
        }
        return output;
    }
    
    private TokenLine setTypes(TokenLine line) {
        Token last = null;
        int index;
        List<Token> tokens = line.getTokens();
        if (tokens.get(0).getRaw().startsWith(COMMENT)) {
            tokens.get(0).setType(TokenType.COMMENT);
            return line;
        }
        for (int i = 0; i < tokens.size(); ++i) {
            Token token = tokens.get(i);
            char first = token.getRaw().charAt(0);
            int type;
            if (token.getRaw().startsWith(COMMENT)) type = TokenType.COMMENT;
            else if (Character.isLetter(first) || first == '_') type = TokenType.IDENTIFIER;
            else if (Character.isDigit(first)) {
                type = TokenType.NUMBER;
                if (i + 1 < tokens.size()) {
                    Token next = tokens.get(i + 1);
                    if (next.getRaw().equals(".")) {
                        int offset = 1;
                        token.append(next);
                        if (i + 2 < tokens.size()) {
                            next = tokens.get(i + 2);
                            if (Character.isDigit(next.getRaw().charAt(0))) {
                                offset = 2;
                                token.append(next);
                            }
                        }
                        while (offset > 0) {
                            tokens.remove(i + 1);
                            offset--;
                        }
                    }
                }
            }
            else if ((index = PAIRS.indexOf(first)) > -1) {
                if (evenIndex(index)) {
                    type = TokenType.GROUP_OPEN;
                    if (first == '(' && last != null && last.getType() == TokenType.IDENTIFIER) {
                        last.setType(TokenType.CALL);
                    }
                }
                else type = TokenType.GROUP_CLOSE;
            }
            else if (GROUPS.indexOf(first) > -1) type = TokenType.STRING;
            else type = TokenType.OPERATOR;
            token.setType(type);
            last = token;
        }
        return line;
    }
    
    private static boolean evenIndex(int n) {
        return (n & 1) == 0; // or mod 2, whatever works
    }
    
}
