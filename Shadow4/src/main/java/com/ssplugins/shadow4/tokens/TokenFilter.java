package com.ssplugins.shadow4.tokens;

import java.util.function.BiConsumer;

public interface TokenFilter extends BiConsumer<String, TokenBuilder> {
    
    static TokenFilter standardTokenFilter() {
        return (s, builder) -> {
            if (builder.getCurrentType() == TokenType.IDENTIFIER &&
                    (builder.getCurrentToken().equals("true") || builder.getCurrentToken().equals("false"))) {
                builder.setCurrentType(TokenType.BOOLEAN);
            }
        };
    }
    
}
