package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.ShadowParser;
import com.ssplugins.shadow3.exception.NamedShadowException;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.parsing.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Call extends ShadowSection {
    
    private ShadowSection[] parameters;
    
    public Call(TokenLine line, Token[] tokens, ShadowParser.Parser parser) {
        super(line, tokens);
        List<ShadowSection> params = new ArrayList<>();
        int start = 2;
        for (int i = 2, end = tokens.length - 1; i < end; ++i) {
            Token token = tokens[i];
            if (token.getType() == TokenType.GROUP_OPEN) {
                int last = ShadowParser.findGroupEnd(tokens, i, end);
                if (last == -1) throw new NamedShadowException("ParseError", line, token.getIndex(), "Unable to find closing token.");
                if (last == -2) throw new NamedShadowException("ParseError", line, token.getIndex(), "Too many closing tokens.");
                i = last;
            }
            else if (token.getRaw().equals(",")) {
                if (i - start == 0) throw new NamedShadowException("ParseError", line, token.getIndex(), "Missing parameter value.");
                Token[] paramTokens = Arrays.copyOfRange(tokens, start, i, Token[].class);
                params.add(parser.toSection(paramTokens));
                start = i + 1;
            }
        }
        parameters = params.toArray(new ShadowSection[0]);
    }
    
    public String getName() {
        return getPrimaryToken().getRaw();
    }
    
    public ShadowSection[] getParameters() {
        return parameters;
    }
    
}
