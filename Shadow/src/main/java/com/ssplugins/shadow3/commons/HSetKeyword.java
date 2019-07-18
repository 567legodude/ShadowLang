package com.ssplugins.shadow3.commons;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HSetKeyword extends CommandKeyword<Set, HSetKeyword.SetTransformer> {
    
    @SuppressWarnings("unchecked")
    public HSetKeyword() {
        super(Set.class, SetTransformer.class, new Range.Any(), "hset");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        Returnable setReturn = Returnable.of(Set.class);
    
        SubKeyword aNew = new SubKeyword("new", new Range.None());
        aNew.setAction((keyword, stepper, scope) -> {
            return (SetTransformer) input -> new HashSet<>();
        });
        aNew.setReturnable(setReturn);
        aNew.setCommandGen((input, c, keyword, type, method) -> {
            String name = c.getScope().nextTemp();
            method.addStatement("$T $L = new $T<>()", Set.class, name, HashSet.class);
            return name;
        });
        context.addKeyword(aNew);
    
        SubKeyword add = new SubKeyword("add", new Range.Single(1));
        add.setAction((keyword, stepper, scope) -> {
            return (SetTransformer) input -> {
                input.add(keyword.argumentValue(0, scope));
                return input;
            };
        });
        add.setReturnable(setReturn);
        add.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection value = keyword.getArguments().get(0);
            method.addStatement("$L.add($L)", input, value.getGeneration(c, type, method));
            return input;
        });
        context.addKeyword(add);
    
        SubKeyword remove = new SubKeyword("remove", new Range.LowerBound(1));
        remove.setAction((keyword, stepper, scope) -> {
            return (SetTransformer) input -> {
                input.removeAll(keyword.argumentValues(scope));
                return input;
            };
        });
        remove.setReturnable(setReturn);
        remove.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 1) {
                method.addStatement("$L.remove($L)", input, args.get(0).getGeneration(c, type, method));
            }
            else {
                method.addStatement("$L.removeAll($L.asList($L))", input, Arrays.class, JavaGen.toArgs(args, c, type, method));
            }
            return input;
        });
        context.addKeyword(remove);
    
        SubKeyword contains = new SubKeyword("contains", new Range.LowerBound(1));
        contains.setAction((keyword, stepper, scope) -> {
            return (SetTransformer) input -> {
                return input.containsAll(keyword.argumentValues(scope));
            };
        });
        contains.setReturnable(Returnable.of(Boolean.class));
        contains.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 1) {
                return CodeBlock.of("$L.contains($L)", input, args.get(0).getGeneration(c, type, method)).toString();
            }
            else {
                return CodeBlock.of("$L.containsAll($T.asList($L))", input, Arrays.class, JavaGen.toArgs(args, c, type, method)).toString();
            }
        });
        context.addKeyword(contains);
    
        SubKeyword size = new SubKeyword("size", new Range.None());
        size.setAction((keyword, stepper, scope) -> {
            return (SetTransformer) Set::size;
        });
        size.setReturnable(Returnable.of(Integer.class));
        size.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return CodeBlock.of("$L.size()", input).toString();
        });
        context.addKeyword(size);
    
        SubKeyword clear = new SubKeyword("clear", new Range.None());
        clear.setAction((keyword, stepper, scope) -> {
            return (SetTransformer) input -> {
                input.clear();
                return input;
            };
        });
        clear.setReturnable(setReturn);
        clear.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            method.addStatement("$L.clear()", input);
            return input;
        });
        context.addKeyword(clear);
    }
    
    @Override
    protected Object onEmptyArguments(Keyword keyword, Stepper stepper, Scope scope) {
        return new HashSet<>();
    }
    
    @Override
    protected Object processArgument(ShadowSection section, int index, Scope scope) {
        if (section instanceof Identifier) {
            return (SetTransformer) input -> Transformer.getter(Set.class, section, scope);
        }
        return super.processArgument(section, index, scope);
    }
    
    @Override
    protected Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<SetTransformer> input) {
        return useTransform(keyword, input);
    }
    
    @Override
    protected Class<?> getReturnType(List<ShadowSection> args, CompileScope scope) {
        if (args.size() == 0) return Set.class;
        ShadowSection section = args.get(args.size() - 1);
        if (section instanceof Identifier) {
            return TypeChecker.check(scope, section).is(Set.class).orError();
        }
        return super.getReturnType(args, scope);
    }
    
    @Override
    protected String generateNoArgs(GenerateContext context, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method) {
        return CodeBlock.of("new $T<>()", HashSet.class).toString();
    }
    
    @Override
    protected String generateSingle(GenerateContext context, String value, ShadowSection section, TypeSpec.Builder type, MethodSpec.Builder method) {
        if (section instanceof Identifier) {
            TypeChecker.require(context.getScope(), section, Set.class);
            return section.getGeneration(context, type, method);
        }
        return super.generateSingle(context, value, section, type, method);
    }
    
    public interface SetTransformer extends Transformer<Set> {}
    
}
