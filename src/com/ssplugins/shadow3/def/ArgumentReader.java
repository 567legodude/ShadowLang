package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

import java.util.function.BiConsumer;

public interface ArgumentReader extends BiConsumer<Block, TokenReader> {
    
    static ArgumentReader values(int amount) {
        return (block, reader) -> {
            // TODO
        };
    }
    
    static ArgumentReader pattern(TokenType... types) {
        return (block, reader) -> {
            if (types.length == 0) {
                if (reader.hasNext()) {
                    throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Expected no modifiers here.");
                }
                return;
            }
            for (TokenType type : types) {
                if (!reader.nextMatches(type, null)) {
                    throw new ShadowParseError(reader.getLine(), reader.peekNext().getIndex(), "Expected " + type.name() + ", found " + reader.nextType().name() + ".");
                }
                reader.nextSection();
            }
        };
    }
    
}
