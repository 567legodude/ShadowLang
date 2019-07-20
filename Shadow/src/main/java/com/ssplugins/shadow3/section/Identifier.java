package com.ssplugins.shadow3.section;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.exception.NamedShadowException;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.util.CompileScope;

import javax.lang.model.SourceVersion;
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
        return scope.get(getName()).orElseThrow(error).value();
    }
    
    @Override
    public Class<?> getReturnType(CompileScope scope) {
        return scope.get(getName()).orElseThrow(error);
    }
    
    @Override
    public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder builder) {
        return getName();
    }
    
    public String getName() {
        return getPrimaryToken().getRaw();
    }
    
    public String getValidName() {
        if (!SourceVersion.isName(getName())) {
            throw new ShadowParseError(getLine(), index(), "Identifier is not a valid name in Java.");
        }
        return getName();
    }
    
}
