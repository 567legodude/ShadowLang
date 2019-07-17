package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.ShadowSection;
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
    }
    
    protected abstract Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<T> input);
    
    protected Object transformInput(T input, I data) {return null;}
    
    protected Object processArgument(ShadowSection section, int index, Scope scope) {
        return section.toObject(scope);
    }
    
    protected Object onEmptyArguments(Keyword keyword, Stepper stepper, Scope scope) {
        return null;
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
    
}
