package com.ssplugins.shadow3.commons;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.compile.TypeChecker;
import com.ssplugins.shadow3.def.Returnable;
import com.ssplugins.shadow3.def.custom.CommandKeyword;
import com.ssplugins.shadow3.def.custom.SubKeyword;
import com.ssplugins.shadow3.def.custom.Transformer;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.Compound;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowNumber;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.OperatorTree;
import com.ssplugins.shadow3.util.Range;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayKeyword extends CommandKeyword<Object, ArrayKeyword.ArrayTransformer> {
    
    public ArrayKeyword() {
        super(Object.class, ArrayTransformer.class, new Range.LowerBound(1), "array");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        Returnable arrReturn = Returnable.of(Object[].class);
    
        SubKeyword aNew = new SubKeyword("new", new Range.Single(1));
        aNew.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) input -> {
                int size = keyword.getInt(0, scope);
                return Array.newInstance(Object.class, size);
            };
        });
        aNew.setReturnable(arrReturn);
        aNew.setCommandGen((input, c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Integer.class);
            String name = c.getScope().nextTemp();
            method.addStatement("$T[] $L = new $T[$L]", Object.class, name, Object.class, section.getGeneration(c, type, method));
            return name;
        });
        context.addKeyword(aNew);
    
        SubKeyword get = new SubKeyword("get", new Range.Single(1));
        get.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) input -> {
                int index = keyword.getInt(0, scope);
                return Array.get(input, index);
            };
        });
        get.setReturnable(Returnable.any());
        get.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Integer.class);
            return CodeBlock.of("$L[$L]", input, section.getGeneration(c, type, method)).toString();
        });
        context.addKeyword(get);
    
        SubKeyword swap = new SubKeyword("swap", new Range.Single(2));
        swap.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) input -> {
                int i1 = keyword.getInt(0, scope);
                int i2 = keyword.getInt(1, scope);
                Object v = Array.get(input, i1);
                Array.set(input, i1, Array.get(input, i2));
                Array.set(input, i2, v);
                return input;
            };
        });
        swap.setReturnable(arrReturn);
        swap.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            ShadowSection i1 = args.get(0);
            ShadowSection i2 = args.get(1);
            TypeChecker.require(c.getScope(), i1, Integer.class);
            TypeChecker.require(c.getScope(), i2, Integer.class);
            String ind1 = i1.getGeneration(c, type, method);
            String ind2 = i2.getGeneration(c, type, method);
            String tmp = c.getScope().nextTemp();
            method.addStatement("$T $L = $L[$L]", Object.class, tmp, input, ind1);
            method.addStatement("$L[$L] = $L[$L]", input, ind1, input, ind2);
            method.addStatement("$L[$L] = $L", input, ind2, tmp);
            return input;
        });
        context.addKeyword(swap);
    
        SubKeyword len = new SubKeyword("len", new Range.None());
        len.setAction((keyword, stepper, scope) -> {
            return (ArrayTransformer) Array::getLength;
        });
        len.setReturnable(Returnable.of(Integer.class));
        len.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return CodeBlock.of("$L.length", input).toString();
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
    protected Class<?> getReturnType(List<ShadowSection> args, CompileScope scope) {
        ShadowSection section = args.get(args.size() - 1);
        if (section instanceof Identifier) {
            return TypeChecker.check(scope, section).type(Object[].class).isArray().orError();
        }
        if (section instanceof ShadowNumber) {
            return TypeChecker.check(scope, section).is(Integer.class).type(Object[].class).orError();
        }
        if (section instanceof Compound) {
            return TypeChecker.check(scope, section).is(IndexAssignment.class).type(Object[].class).orError();
        }
        return super.getReturnType(args, scope);
    }
    
    @Override
    protected String generateSingle(GenerateContext context, String value, ShadowSection section, TypeSpec.Builder type, MethodSpec.Builder method) {
        if (section instanceof Identifier) {
            TypeChecker.check(context.getScope(), section).isArray().orError();
            return section.getGeneration(context, type, method);
        }
        if (section instanceof ShadowNumber) {
            TypeChecker.require(context.getScope(), section, Integer.class);
            String name = context.getScope().nextTemp();
            method.addStatement("$T[] $L = new $T[$L]", Object.class, name, Object.class, section.getGeneration(context, type, method));
            return name;
        }
        if (section instanceof Compound) {
            requireValue(value, section);
            TypeChecker.require(context.getScope(), section, IndexAssignment.class);
            OperatorTree.OpNode root = (OperatorTree.OpNode) ((Compound) section).getOpTree().getRoot();
            OperatorTree.Node[] c = root.getChildren();
            method.addStatement("$L[$L] = $L", value, c[0].getGeneration(context, type, method), c[1].getGeneration(context, type, method));
            return value;
        }
        return super.generateSingle(context, value, section, type, method);
    }
    
    public interface ArrayTransformer extends Transformer<Object> {}
    
}
