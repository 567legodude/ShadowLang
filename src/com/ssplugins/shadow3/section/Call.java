package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.util.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Call extends ShadowSection {
    
    private static final Schema<Token> paramSplitter = Token.matcher().either(Token.is(TokenType.OPERATOR, ","), Token.is(TokenType.GROUP_CLOSE, ")"));
    
    private Identifier name;
    private List<ShadowSection> parameters;
    
    public Call(TokenReader reader) {
        super(reader.getLine());
        parameters = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();
        
        name = (Identifier) reader.readAs(TokenType.IDENTIFIER);
        tokens.add(name.getPrimaryToken());
        tokens.add(reader.expect(TokenType.GROUP_OPEN, "("));
        if (!reader.nextMatches(TokenType.GROUP_CLOSE, ")")) {
            while (reader.hasNext()) {
                ShadowSection param = reader.readCompoundValue(paramSplitter, "\",\" or \")\"");
                parameters.add(param);
                tokens.addAll(Arrays.asList(param.getTokens()));
                if (reader.nextMatches(TokenType.GROUP_CLOSE, ")")) break;
                tokens.add(reader.expect(TokenType.OPERATOR, ","));
            }
        }
        tokens.add(reader.expect(TokenType.GROUP_CLOSE, ")"));
        
        setTokens(tokens.toArray(new Token[0]));
    }
    
    @Override
    public Object toObject(Scope scope) {
        throw ShadowException.sectionExec(this, "Function calls are not implemented yet.").get();
    }
    
    public String getName() {
        return getPrimaryToken().getRaw();
    }
    
    public List<ShadowSection> getParameters() {
        return parameters;
    }
    
}
