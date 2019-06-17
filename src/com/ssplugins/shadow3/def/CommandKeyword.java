package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandKeyword<T, D> extends KeywordType {
    
    private Class<T> dataType;
    private Class<D> inputType;
    
    public CommandKeyword(Class<T> dataType, Class<D> inputType, Range arguments, String name) {
        super(name, arguments);
        this.dataType = dataType;
        this.inputType = inputType;
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
                if (!dataType.isAssignableFrom(object.getClass())) {
                    throw new ShadowExecutionError(keyword.getLine(), keyword.getArguments().get(i).getPrimaryToken().getIndex(), "Argument value must be an object specific to this keyword.");
                }
                input.add(dataType.cast(object));
            }
            return onExecute(keyword, stepper, scope, input);
        });
    }
    
    protected abstract Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<T> input);
    
    protected Object transformInput(T input, D data) {return null;}
    
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
    
    public interface Transformer<T> {
    
        Object transform(T t);
    
        static <U> U getter(Class<U> expected, ShadowSection section, Scope scope) {
            Object o = section.toObject(scope);
            if (!expected.isInstance(o)) {
                throw new ShadowExecutionError(section.getLine(), section.getPrimaryToken().getIndex(), "Argument is not the correct type for this keyword.");
            }
            return expected.cast(o);
        }
        
    }
    
}
