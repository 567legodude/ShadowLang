package com.ssplugins.shadow3.def.custom;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.Returnable;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.InlineKeyword;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandKeyword<I, T extends Transformer<I>> extends KeywordType {
    
    private Class<I> inputType;
    private Class<T> transformType;
    
    public CommandKeyword(Class<I> inputType, Class<T> transformType, Range arguments, String name) {
        super(name, arguments);
        this.inputType = inputType;
        this.transformType = transformType;
        super.setAction((keyword, stepper, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 0) return onEmptyArguments(keyword, stepper, scope);
            List<Object> objects = new ArrayList<>(args.size());
            for (int i = 0; i < args.size(); ++i) {
                objects.add(processArgument(args.get(i), i, scope));
            }
            
            List<T> input = new ArrayList<>();
            for (int i = 0; i < objects.size(); ++i) {
                Object object = objects.get(i);
                if (!transformType.isAssignableFrom(object.getClass())) {
                    throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(i), "Argument value must be an object specific to this keyword.");
                }
                input.add(transformType.cast(object));
            }
            return onExecute(keyword, stepper, scope, input);
        });
        super.setReturnable((keyword, scope) -> getReturnType(keyword.getArguments(), scope));
        super.setGenerator(this::generate);
    }
    
    protected abstract Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<T> input);
    
    protected Object transformInput(T input, I data) {return null;}
    
    protected Object processArgument(ShadowSection section, int index, Scope scope) {
        return section.toObject(scope);
    }
    
    protected Object onEmptyArguments(Keyword keyword, Stepper stepper, Scope scope) {
        return null;
    }
    
    protected Class<?> getReturnType(List<ShadowSection> args, CompileScope scope) {
        if (args.size() == 0) return Returnable.empty();
        return args.get(args.size() - 1).getReturnType(scope);
    }
    
    protected String generate(GenerateContext context, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method) {
        if (keyword.getArguments().size() == 0) {
            return generateNoArgs(context, keyword, type, method);
        }
        String value = null;
        for (ShadowSection section : keyword.getArguments()) {
            value = generateSingle(context, value, section, type, method);
        }
        if (value == null) {
            throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(-1), "Keyword did not generate any code.");
        }
        return value;
    }
    
    protected String generateSingle(GenerateContext context, String value, ShadowSection section, TypeSpec.Builder type, MethodSpec.Builder method) {
        if (!(section instanceof InlineKeyword)) {
            throw new ShadowParseError(section.getLine(), section.index(), "Unsupported input type.");
        }
        Keyword k = ((InlineKeyword) section).getKeyword();
        KeywordType def = getLookupContext().findKeyword(k.getName())
                                            .filter(t -> t == k.getDefinition())
                                            .orElseThrow(() -> new ShadowParseError(section.getLine(), section.index(), "Unknown command keyword."));
        if (!(def instanceof SubKeyword)) {
            throw new ShadowParseError(section.getLine(), section.index(), "Invalid keyword definition.");
        }
        return ((SubKeyword) def).getCommandGen().generate(value, context, k, type, method);
    }
    
    protected String generateNoArgs(GenerateContext context, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method) {
        throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(-1), "Keyword action is undefined.");
    }
    
    protected final Object useTransform(Keyword keyword, List<T> input) {
        Object value = null;
        for (int i = 0; i < input.size(); i++) {
            if (value != null && !inputType.isInstance(value)) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(i), "Argument received unknown input object.");
            }
            value = transformInput(input.get(i), inputType.cast(value));
        }
        return value;
    }
    
    protected void requireValue(String input, Keyword keyword) {
        if (input == null) {
            throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(-1), "Argument received no input.");
        }
    }
    
}
