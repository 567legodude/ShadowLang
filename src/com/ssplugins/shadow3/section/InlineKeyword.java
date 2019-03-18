package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenSchema;
import com.ssplugins.shadow3.parsing.TokenType;

public class InlineKeyword extends ShadowSection {
    
    public static final TokenSchema SCHEMA = createTokenSchema();
    
    public InlineKeyword(TokenLine line, Token[] tokens) {
        super(line, tokens);
    }
    
    public static TokenSchema createTokenSchema() {
        TokenSchema schema = new TokenSchema()
                .minLength(3)
                .type(TokenType.GROUP_OPEN, "[")
                .typeLast(TokenType.GROUP_CLOSE, "]");
        return schema;
    }
    
}
