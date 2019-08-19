package com.ssplugins.shadow3.entity;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.BlockEffector;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.def.*;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.LineReader;
import com.ssplugins.shadow3.util.Parameter;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Block extends ShadowEntity {
    
    private String name;
    private List<ShadowSection> modifiers;
    private List<Parameter> parameters;
    private EntityList contents;
    
    private BlockType definition;
    private ShadowContext innerContext;
    
    private BlockEffector effector;
    private boolean checked;
    private Class<?> declaredType;
    private Class<?> returnType;
    
    public Block(Block parent, LineReader reader) {
        super(reader.next(), parent);
        setTopContext(reader.getContext());
        TokenReader def = new TokenReader(this, reader.getParser(), getLine());
        setIndex(def.getIndex());
        name = def.expect(TokenType.IDENTIFIER).getRaw();
        
        definition = findDef(parent, reader.getContext());
        
        def.setLimit(getLine().getBlockEnd());
        modifiers = new ArrayList<>();
        while (def.hasNext() && !def.nextMatches(TokenType.OPERATOR, "->")) {
            modifiers.add(def.nextSection());
        }
        
        parameters = new ArrayList<>();
        if (def.nextMatches(TokenType.OPERATOR, "->")) {
            def.consume();
            parameters.add(readParameter(def, reader.getContext()));
            while (def.hasNext()) {
                def.expect(TokenType.OPERATOR, ",");
                parameters.add(readParameter(def, reader.getContext()));
            }
        }
        def.reset();
        
        Range mods = definition.getModifiers();
        if (!mods.contains(modifiers.size())) {
            throw new ShadowParseError(getLine(), getLine().firstToken().getIndex(), "Block expects " + mods.toString("modifier") + ", found " + modifiers.size());
        }
        Range params = definition.getParameters();
        if (!params.contains(parameters.size())) {
            throw new ShadowParseError(getLine(), getLine().firstToken().getIndex(), "Block expects " + params.toString("parameter") + ", found " + parameters.size());
        }
    
        ParseCallback<Block> parseCallback = definition.getParseCallback();
        if (parseCallback != null) parseCallback.onParse(this, getEffectiveContext());
        innerContext = definition.getContextTransformer().get(this, reader.getContext(), getEffectiveContext());
        runCompleteCallbacks();
        
        contents = new EntityList();
        if (def.hasNext() && def.nextMatches(TokenType.OPERATOR, "::")) {
            def.consume();
            Keyword keyword = reader.getParser().readKeyword(this, def, reader.getContext());
            contents.add(keyword);
        }
        else {
            def.expect(TokenType.GROUP_OPEN, "{");
            while (reader.hasNext() && !reader.nextIsClose()) {
                contents.add(reader.nextEntity(this, ShadowCodeException.noClose(getLine(), getLine().lastToken().getIndex(), "Reached end of file while searching for closing bracket.")));
            }
            reader.consume(ShadowCodeException.noClose(getLine(), getLine().firstToken().getIndex(), "No closing bracket found for block."));
        }
    }
    
    private BlockType findDef(Block parent, ShadowContext fallback) {
        // Check lookup context, then search parent tree for inner context.
        if (parent != null) {
            ShadowContext lookupContext = parent.getDefinition().getLookupContext();
            if (lookupContext != null) {
                Optional<BlockType> block = lookupContext.findBlock(name);
                if (block.isPresent()) {
                    setFrom(parent);
                    return block.get();
                }
            }
        }
        while (parent != null) {
            Optional<BlockType> block = checkContext(parent.getInnerContext());
            if (block.isPresent()) {
                setFrom(parent);
                return block.get();
            }
            parent = (Block) parent.getParent();
        }
        return fallback.findBlock(name).orElseThrow(ShadowCodeException.noDef(getLine(), getLine().firstToken().getIndex(), "No definition found for block: " + name));
    }
    
    private Parameter readParameter(TokenReader def, ShadowContext context) {
        Identifier identifier = def.expectSection(Identifier.class, "identifier");
        identifier.getValidName();
        Identifier typeDef = null;
        int dim = 0;
        if (def.nextMatches(TokenType.OPERATOR, ":")) {
            def.consume();
            typeDef = def.expectSection(Identifier.class, "identifier");
            while (def.nextMatches(TokenType.GROUP_OPEN, "[")) {
                def.consume();
                def.expect(TokenType.GROUP_CLOSE, "]");
                dim++;
            }
        }
        if (typeDef != null) {
            Class<?> type = context.findType(typeDef.getName()).orElseThrow(ShadowCodeException.noDef(getLine(), typeDef.getPrimaryToken().getIndex(), "Type not defined."));
            if (dim > 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < dim; i++) {
                    builder.append("[");
                }
                builder.append("L").append(type.getName()).append(";");
                try {
                    Class<?> arrType = Class.forName(builder.toString());
                    return new Parameter(identifier, arrType);
                } catch (ClassNotFoundException e) {
                    throw new ShadowParseError(getLine(), identifier.getPrimaryToken().getIndex(), "Cannot make array of type: " + type.getName());
                }
            }
            return new Parameter(identifier, type);
        }
        return new Parameter(identifier);
    }
    
    private Optional<BlockType> checkContext(ShadowContext context) {
        return Optional.ofNullable(context).flatMap(c -> c.findBlock(name));
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        PreRunCheck preRunCheck = definition.getPreRunCheck();
        if (preRunCheck != null && !preRunCheck.willEnter(this, scope, args)) return null;
        BlockEnterCallback enterCallback = definition.getEnterCallback();
        if (enterCallback != null) enterCallback.onEnter(this, stepper, scope, args);
        Stepper contentStepper = new Stepper(stepper, innerContext, this);
        Scope innerScope = scope.makeLevel(contentStepper);
        contentStepper.setScope(innerScope);
        contentStepper.run(stpr -> {
            BlockEndCallback endCallback = definition.getEndCallback();
            if (endCallback != null) endCallback.onEnd(this, stpr, scope);
        });
        Object r = innerScope.getReturnValue();
        innerScope.clean();
        return r;
    }
    
    @Override
    public void addArgument(ShadowSection section) {
        modifiers.add(section);
    }
    
    @Override
    public List<ShadowSection> getArguments() {
        return modifiers;
    }
    
    @Override
    public ShadowContext getInnerContext() {
        return innerContext;
    }
    
    public void run() {
        Stepper stepper = new Stepper(null, getTopContext(), this);
        stepper.run();
        stepper.getScope().runCallbacks();
    }
    
    public Object run(List<Object> params) {
        ShadowContext context = getTopContext();
        Stepper stepper = new Stepper(null, context, this);
        Scope scope = new Scope(context, stepper);
        Object value = this.execute(stepper, scope, params);
        scope.runCallbacks();
        return value;
    }
    
    @Override
    public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        context.newBlock();
        if (effector != null) effector.apply(this, context.getScope());
        getDefinition().getGenerator().generate(context, this, type, method);
        context.back();
        return null;
    }
    
    public void findParameterTypes(CompileScope scope) {
        ParamLookup lookup = definition.getParamLookup();
        if (lookup != null) {
            List<Parameter> parameterList = getParameters();
            for (int i = 0; i < parameterList.size(); i++) {
                Parameter p = parameterList.get(i);
                Class<?> paramType = lookup.getParamType(i, this);
                if (paramType == null || paramType == Object.class) continue;
                p.setType(paramType);
            }
        }
        getParameters().forEach(p -> {
            scope.mark(p.getName());
            scope.addCheck(p.getName(), p.getType());
        });
    }
    
    public void generateCode(TypeSpec.Builder type, MethodSpec.Builder method) {
        getGeneration(new GenerateContext(getEffectiveContext()), type, method);
    }
    
    public void addBody(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        context.newBlock();
        getParameters().forEach(p -> {
            context.getScope().mark(p.getName());
            context.getScope().addCheck(p.getName(), p.getType());
        });
        getContents().forEach(entity -> entity.getGeneration(context, type, method));
        context.back();
    }
    
    public List<ShadowSection> getModifiers() {
        return modifiers;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    public EntityList getContents() {
        return contents;
    }
    
    public BlockType getDefinition() {
        return definition;
    }
    
    public void setEffector(BlockEffector effector) {
        this.effector = effector;
    }
    
    public boolean isChecked() {
        return checked;
    }
    
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    
    public Class<?> getDeclaredType() {
        return declaredType;
    }
    
    public void setDeclaredType(Class<?> declaredType) {
        this.declaredType = declaredType;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
    
}
