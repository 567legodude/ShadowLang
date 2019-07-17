package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.custom.CommandKeyword;
import com.ssplugins.shadow3.def.custom.Transformer;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.Compound;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowNumber;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Range;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayKeyword extends CommandKeyword<Object, ArrayKeyword.ArrayTransformer> {
    
    public ArrayKeyword() {
        super(Object.class, ArrayTransformer.class, new Range.LowerBound(1), "array");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        KeywordType aNew = new KeywordType("new", new Range.Single(1));
        aNew.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) input -> {
                Integer size = keyword.getArgument(0, Integer.class, scope, "Argument must be an integer.");
                return Array.newInstance(Object.class, size);
            };
        });
        context.addKeyword(aNew);
    
        KeywordType get = new KeywordType("get", new Range.Single(1));
        get.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) input -> {
                Integer index = keyword.getArgument(0, Integer.class, scope, "Argument must be an integer.");
                return Array.get(input, index);
            };
        });
        context.addKeyword(get);
    
        KeywordType swap = new KeywordType("swap", new Range.Single(2));
        swap.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) input -> {
                Integer i1 = keyword.getArgument(0, Integer.class, scope, "Argument must be an integer.");
                Integer i2 = keyword.getArgument(1, Integer.class, scope, "Argument must be an integer.");
                Object v = Array.get(input, i1);
                Array.set(input, i1, Array.get(input, i2));
                Array.set(input, i2, v);
                return input;
            };
        });
        context.addKeyword(swap);
    
        KeywordType len = new KeywordType("len", new Range.None());
        len.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) Array::getLength;
        });
        context.addKeyword(len);
    }
    
    @Override
    protected Object processArgument(ShadowSection section, int index, Scope scope) {
        if (section instanceof Identifier) {
            return (ArrayTransformer) input -> {
                Object o = section.toObject(scope);
                if (!o.getClass().isArray()) {
                    throw new ShadowExecutionError(section.getLine(), section.getPrimaryToken().getIndex(), "Argument is not an array.");
                }
                return o;
            };
        }
        else if (section instanceof ShadowNumber) {
            return (ArrayTransformer) input -> {
                Number n = (Number) section.toObject(scope);
                if (!(n instanceof Integer)) {
                    throw new ShadowExecutionError(section.getLine(), section.getPrimaryToken().getIndex(), "Argument is not an integer.");
                }
                if (input == null) return Array.newInstance(Object.class, n.intValue());
                return Array.get(input, n.intValue());
            };
        }
        else if (section instanceof Compound) {
            return (ArrayTransformer) input -> {
                Object o = section.toObject(scope);
                if (!(o instanceof IndexAssignment)) {
                    throw new ShadowExecutionError(section.getLine(), section.getPrimaryToken().getIndex(), "Argument is not an index assignment.");
                }
                IndexAssignment a = (IndexAssignment) o;
                Array.set(input, a.getIndex(), a.getValue());
                return input;
            };
        }
        return super.processArgument(section, index, scope);
    }
    
    @Override
    protected Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<ArrayTransformer> input) {
        return useTransform(keyword, input);
    }
    
    @Override
    protected Object transformInput(ArrayTransformer input, Object data) {
        return input.transform(data);
    }
    
    public interface ArrayTransformer extends Transformer<Object> {}
    
}
