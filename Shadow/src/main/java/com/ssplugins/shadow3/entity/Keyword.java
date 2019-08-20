package com.ssplugins.shadow3.entity;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.compile.KeywordEffector;
import com.ssplugins.shadow3.def.KeywordAction;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.ParseCallback;
import com.ssplugins.shadow3.exception.NamedShadowException;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Keyword extends ShadowEntity {
    
    private String name;
    private List<ShadowSection> arguments;
    
    private KeywordType definition;
    private ShadowContext innerContext;
    
    private Class<?> returnType;
    private KeywordEffector effector;
    
    public Keyword(ShadowEntity parent, TokenReader def, ShadowContext fallback) {
        super(def.getLine(), parent);
        setTopContext((parent == null ? fallback : parent.getTopContext()));
        setIndex(def.getIndex());
        name = def.readAs(TokenType.IDENTIFIER).getPrimaryToken().getRaw();
    
        definition = findDef(parent, fallback);
        
        arguments = new ArrayList<>();
        while (def.hasNext()) {
            def.setParent(this);
            arguments.add(def.nextSection());
        }
    
        Range args = definition.getArguments();
        if (!args.contains(arguments.size())) {
            throw new NamedShadowException("", getLine(), argumentIndex(-1), "Keyword expects " + args.toString("argument") + ", found " + arguments.size());
        }
    
        ParseCallback<Keyword> parseCallback = definition.getParseCallback();
        if (parseCallback != null) parseCallback.onParse(this, getEffectiveContext());
        innerContext = definition.getContextTransformer().get(this, fallback, getEffectiveContext());
        runCompleteCallbacks();
    }
    
    private KeywordType findDef(ShadowEntity parent, ShadowContext fallback) {
        // Check lookup context, then search parent tree for inner context.
        if (parent != null) {
            ShadowContext lookupContext = null;
            if (parent instanceof Block) lookupContext = ((Block) parent).getDefinition().getLookupContext();
            else if (parent instanceof Keyword) lookupContext = ((Keyword) parent).getDefinition().getLookupContext();
            if (lookupContext != null) {
                Optional<KeywordType> keyword = lookupContext.findKeyword(name);
                if (keyword.isPresent()) {
                    setFrom(parent);
                    return keyword.get();
                }
            }
        }
        while (parent != null) {
            Optional<KeywordType> keyword = checkContext(parent.getInnerContext());
            if (keyword.isPresent()) {
                setFrom(parent);
                return keyword.get();
            }
            parent = parent.getParent();
        }
        return fallback.findKeyword(name).orElseThrow(ShadowCodeException.noDef(getLine(), argumentIndex(-1), "No definition found for keyword: " + name));
    }
    
    private Optional<KeywordType> checkContext(ShadowContext context) {
        return Optional.ofNullable(context).flatMap(c -> c.findKeyword(name));
    }
    
    public static Schema<Keyword> inlineOnly() {
        Schema<Keyword> s = new Schema<>(ShadowEntity::isInline);
        s.setSituation("Keyword can only be used inline.");
        return s;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        KeywordAction action = definition.getAction();
        if (action == null) {
            throw new ShadowCodeException(getLine(), getLine().firstToken().getIndex(), "Keyword has no defined action.");
        }
        return action.execute(this, stepper, scope);
    }
    
    @Override
    public void addArgument(ShadowSection section) {
        arguments.add(section);
    }
    
    @Override
    public List<ShadowSection> getArguments() {
        return arguments;
    }
    
    @Override
    public ShadowContext getInnerContext() {
        return innerContext;
    }
    
    @Override
    public Code getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        Code code = getDefinition().getGenerator().generate(context, this, type, method);
        if (effector != null) effector.apply(this, context.getScope());
        if (!isInline() && getDefinition().isStatementMode()) code.addTo(method);
        return code;
    }
    
    public void findReturnType(CompileScope scope) {
//        if (returnType != null) return;
        returnType = definition.getReturnable().getReturnType(this, scope);
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public void setEffector(KeywordEffector effector) {
        this.effector = effector;
    }
    
    public void setInnerContext(ShadowContext innerContext) {
        this.innerContext = innerContext;
    }
    
    public KeywordType getDefinition() {
        return definition;
    }
    
}
