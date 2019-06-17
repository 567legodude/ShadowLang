package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.exception.NamedShadowException;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;

import java.util.function.Supplier;

public class Identifier extends ShadowSection {
    
    private final Supplier<NamedShadowException> error;
    
    public Identifier(TokenReader reader) {
        super(reader.getLine());
        setToken(reader.expect(TokenType.IDENTIFIER));
        error = ShadowCodeException.section(this, "VariableError", "No variable defined named \"" + getName() + "\"");
    }
    
    @Override
    public Object toObject(Scope scope) {
        return scope.get(getName()).orElseThrow(error);
    }
    
    public String getName() {
        return getPrimaryToken().getRaw();
    }
    
}
