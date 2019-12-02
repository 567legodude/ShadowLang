package com.ssplugins.shadow4.tokens;

import java.util.function.Predicate;

public interface TokenMatcher extends Predicate<Token> {
    
    static TokenMatcher match(TokenType type, String content) {
        return token -> token.matches(type, content);
    }
    
}
