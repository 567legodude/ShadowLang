package com.ssplugins.shadow3.commons;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.compile.JavaGen;
import com.ssplugins.shadow3.compile.TypeChecker;
import com.ssplugins.shadow3.def.Returnable;
import com.ssplugins.shadow3.def.custom.CommandKeyword;
import com.ssplugins.shadow3.def.custom.SubKeyword;
import com.ssplugins.shadow3.def.custom.Transformer;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ListKeyword extends CommandKeyword<List, ListKeyword.ListTransformer> {
    
    @SuppressWarnings("unchecked")
    public ListKeyword() {
        super(List.class, ListTransformer.class, new Range.Any(), "list");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        Returnable listReturn = Returnable.of(List.class);
    
        SubKeyword arraylist = new SubKeyword("arraylist", new Range.None());
        arraylist.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> new ArrayList<>();
        });
        arraylist.setReturnable(listReturn);
        arraylist.setCommandGen((input, c, keyword, type, method) -> {
            String name = c.getScope().nextTemp();
            method.addStatement("$T $L = new $T<>()", List.class, name, ArrayList.class);
            return Code.plain(name);
        });
        context.addKeyword(arraylist);
    
        SubKeyword linkedlist = new SubKeyword("linkedlist", new Range.None());
        linkedlist.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> new LinkedList<>();
        });
        linkedlist.setReturnable(listReturn);
        linkedlist.setCommandGen((input, c, keyword, type, method) -> {
            String name = c.getScope().nextTemp();
            method.addStatement("$T $L = new $T<>()", List.class, name, LinkedList.class);
            return Code.plain(name);
        });
        context.addKeyword(linkedlist);
    
        SubKeyword add = new SubKeyword("add", new Range.LowerBound(1));
        add.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.addAll(keyword.argumentValues(scope));
                return input;
            };
        });
        add.setReturnable(listReturn);
        add.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 1) {
                method.addStatement("$L.add($L)", input, args.get(0).getGeneration(c, type, method));
            }
            else {
                method.addStatement("$L.addAll($T.asList($L))", input, Arrays.class, JavaGen.toArgs(args, c, type, method));
            }
            return input;
        });
        context.addKeyword(add);
    
        SubKeyword set = new SubKeyword("set", new Range.Single(2));
        set.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.set(keyword.getInt(0, scope), keyword.argumentValue(1, scope));
                return input;
            };
        });
        set.setReturnable(listReturn);
        set.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), Integer.class);
            Code index = args.get(0).getGeneration(c, type, method);
            Code value = args.get(1).getGeneration(c, type, method);
            method.addStatement("$L.set($L, $L)", input, index, value);
            return input;
        });
        context.addKeyword(set);
    
        SubKeyword remove = new SubKeyword("remove", new Range.LowerBound(1));
        remove.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.removeAll(keyword.argumentValues(scope));
                return input;
            };
        });
        remove.setReturnable(listReturn);
        remove.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 1) {
                Code.format("$L.remove($L)", input, args.get(0).getGeneration(c, type, method)).addTo(method);
            }
            else {
                Code.format("$L.removeAll($L.asList($L))", input, Arrays.class, JavaGen.toArgs(args, c, type, method)).addTo(method);
            }
            return input;
        });
        context.addKeyword(remove);
    
        SubKeyword removei = new SubKeyword("removei", new Range.Single(1));
        removei.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.remove(keyword.getInt(0, scope));
                return input;
            };
        });
        removei.setReturnable(listReturn);
        removei.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection arg = keyword.getArguments().get(0);
            method.addStatement("$L.remove($L)", input, arg.getGeneration(c, type, method));
            return input;
        });
        context.addKeyword(removei);
    
        SubKeyword get = new SubKeyword("get", new Range.Single(1));
        get.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                int i = keyword.getInt(0, scope);
                return input.get(i);
            };
        });
        get.setReturnable(Returnable.any());
        get.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection index = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), index, Integer.class);
            return Code.format("$L.get($L)", input, index.getGeneration(c, type, method));
        });
        context.addKeyword(get);
    
        SubKeyword contains = new SubKeyword("contains", new Range.LowerBound(1));
        contains.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                return input.containsAll(keyword.argumentValues(scope));
            };
        });
        contains.setReturnable(Returnable.of(Boolean.class));
        contains.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return Code.format("$L.contains($L)", input, keyword.getArguments().get(0).getGeneration(c, type, method));
        });
        context.addKeyword(contains);
    
        SubKeyword size = new SubKeyword("size", new Range.None());
        size.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) List::size;
        });
        size.setReturnable(Returnable.of(Integer.class));
        size.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return Code.format("$L.size()", input);
        });
        context.addKeyword(size);
    
        SubKeyword clear = new SubKeyword("clear", new Range.None());
        clear.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.clear();
                return input;
            };
        });
        clear.setReturnable(listReturn);
        clear.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            method.addStatement("$L.clear()", input);
            return input;
        });
        context.addKeyword(clear);
    }
    
    @Override
    protected Object onEmptyArguments(Keyword keyword, Stepper stepper, Scope scope) {
        return new ArrayList<>();
    }
    
    @Override
    protected Object processArgument(ShadowSection section, int index, Scope scope) {
        if (section instanceof Identifier) {
            return (ListTransformer) input -> Transformer.getter(List.class, section, scope);
        }
        return super.processArgument(section, index, scope);
    }
    
    @Override
    protected Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<ListTransformer> input) {
        return useTransform(keyword, input);
    }
    
    @Override
    protected Class<?> getReturnType(List<ShadowSection> args, CompileScope scope) {
        if (args.size() == 0) return List.class;
        ShadowSection section = args.get(args.size() - 1);
        if (section instanceof Identifier) {
            return TypeChecker.check(scope, section).is(List.class).orError();
        }
        return super.getReturnType(args, scope);
    }
    
    @Override
    protected Code generateNoArgs(GenerateContext context, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method) {
        return Code.format("new $T<>()", ArrayList.class);
    }
    
    @Override
    protected Code generateSingle(GenerateContext context, Code value, ShadowSection section, TypeSpec.Builder type, MethodSpec.Builder method) {
        if (section instanceof Identifier) {
            TypeChecker.require(context.getScope(), section, List.class);
            return section.getGeneration(context, type, method);
        }
        return super.generateSingle(context, value, section, type, method);
    }
    
    public interface ListTransformer extends Transformer<List> {}
    
}
