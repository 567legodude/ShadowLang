package com.ssplugins.shadow4.tokens;

import com.ssplugins.shadow4.context.Context;
import com.ssplugins.shadow4.exception.EOLException;
import com.ssplugins.shadow4.exception.SourceCodeException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tokenizer {
    
    public static final char ESCAPE_CHAR = '\\';
    public static final String STRINGS = "\"'";
    public static final char COMMENT = '#';
    
    private Context context;
    
    public Tokenizer(Context context) {
        this.context = context;
    }
    
    private TokenType hintTokenType(char c, TokenBuilder builder) {
        TokenType type;
        if (Character.isJavaIdentifierStart(c)) {
            type = TokenType.IDENTIFIER;
        }
        else if (Character.isDigit(c)) {
            type = TokenType.NUMBER;
        }
        else {
            type = TokenType.OPERATOR;
        }
        if (builder.isEmptyToken()) {
            builder.hint(type);
        }
        return type;
    }
    
    private void tokenizeBasicTypes(String line, TokenBuilder builder, int lineNumber, char c, Set<String> possibleOps) {
        TokenType actual = hintTokenType(c, builder);
        if (builder.getCurrentType() == TokenType.IDENTIFIER) {
            builder.finishOtherType(actual, lineNumber);
            if (!Character.isJavaIdentifierPart(c) && !builder.isEmptyToken()) {
                throw new SourceCodeException(line, lineNumber, builder.charIndex(), "\'" + c + "\' is not a valid identifier char.");
            }
            builder.addToToken();
        }
        else if (builder.getCurrentType() == TokenType.OPERATOR) {
            builder.finishOtherType(actual, lineNumber);
            builder.addToToken();
            int size = builder.tokenSize();
            possibleOps.removeIf(s -> s.length() < size || s.charAt(size - 1) != c);
            if (possibleOps.size() == 0) {
                throw new SourceCodeException(line, lineNumber, builder.getTokenIndex(), "Unknown operator " + builder.getCurrentToken());
            }
            else if (possibleOps.size() == 1) {
                builder.finishToken(lineNumber);
                possibleOps.addAll(context.operatorSymbols());
            }
        }
        else {
            if (actual == TokenType.OPERATOR && c != '.') {
                builder.finishToken(lineNumber);
            }
            else {
                builder.addToToken();
            }
        }
    }
    
    public List<Token> tokenize(String line, int lineNumber) {
        return tokenize(line, lineNumber, new TokenBuilder(null, TokenFilter.standardTokenFilter()));
    }
    
    public List<Token> tokenize(String line, int lineNumber, TokenBuilder builder) {
        builder.setSource(line);
        Set<String> operators = context.operatorSymbols();
        Set<String> possibleOps = new HashSet<>(operators);
        while (builder.hasNextChar()) {
            char c = builder.nextChar();
            if (c == COMMENT && builder.isEmptyToken()) {
                builder.finishLine();
            }
            else if (STRINGS.indexOf(c) > -1) {
                builder.finishToken(lineNumber);
                builder.hint(TokenType.STRING);
                builder.addToToken();
                if (!builder.addUntil(c, ESCAPE_CHAR)) {
                    throw new EOLException(line, lineNumber, "end of string");
                }
                builder.finishToken(lineNumber);
            }
            else if (Character.isWhitespace(c)) {
                builder.finishToken(lineNumber);
            }
            else if (GroupPair.isPair(c)) {
                builder.finishToken(lineNumber);
                builder.hint(GroupPair.tokenType(c));
                builder.addToToken();
                builder.finishToken(lineNumber);
            }
            else {
                tokenizeBasicTypes(line, builder, lineNumber, c, possibleOps);
            }
        }
        if (!builder.isEmptyToken()) {
            builder.finishToken(lineNumber);
        }
        return builder.getTokens();
    }
    
}
