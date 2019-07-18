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
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapKeyword extends CommandKeyword<Map, MapKeyword.MapTransformer> {
    
    @SuppressWarnings("unchecked")
    public MapKeyword() {
        super(Map.class, MapTransformer.class, new Range.Any(), "map");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        Returnable mapReturn = Returnable.of(Map.class);
    
        SubKeyword aNew = new SubKeyword("new", new Range.None());
        aNew.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> new HashMap<>();
        });
        aNew.setReturnable(mapReturn);
        aNew.setCommandGen((input, c, keyword, type, method) -> {
            String name = c.getScope().nextTemp();
            method.addStatement("$T $L = new $T<>()", Map.class, name, HashMap.class);
            return name;
        });
        context.addKeyword(aNew);
    
        SubKeyword put = new SubKeyword("put", new Range.Single(2));
        put.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> {
                List<Object> objects = keyword.argumentValues(scope);
                input.put(objects.get(0), objects.get(1));
                return input;
            };
        });
        put.setReturnable(mapReturn);
        put.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            List<ShadowSection> args = keyword.getArguments();
            String key = args.get(0).getGeneration(c, type, method);
            String value = args.get(1).getGeneration(c, type, method);
            method.addStatement("$L.put($L, $L)", input, key, value);
            return input;
        });
        context.addKeyword(put);
    
        SubKeyword remove = new SubKeyword("remove", new Range.Single(1));
        remove.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.remove(keyword.argumentValue(0, scope));
        });
        remove.setReturnable(mapReturn);
        remove.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection key = keyword.getArguments().get(0);
            method.addStatement("$L.remove($L)", input, key.getGeneration(c, type, method));
            return input;
        });
        context.addKeyword(remove);
    
        SubKeyword get = new SubKeyword("get", new Range.Single(1));
        get.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.get(keyword.argumentValue(0, scope));
        });
        get.setReturnable(Returnable.any());
        get.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection key = keyword.getArguments().get(0);
            method.addStatement("$L.get($L)", input, key.getGeneration(c, type, method));
            return input;
        });
        context.addKeyword(get);
    
        SubKeyword contains = new SubKeyword("contains", new Range.Single(1));
        contains.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.containsKey(keyword.argumentValue(0, scope));
        });
        contains.setReturnable(Returnable.of(Boolean.class));
        contains.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection key = keyword.getArguments().get(0);
            return CodeBlock.of("$L.containsKey($L)", input, key.getGeneration(c, type, method)).toString();
        });
        context.addKeyword(contains);
    
        SubKeyword hasValue = new SubKeyword("has_value", new Range.Single(1));
        hasValue.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.containsValue(keyword.argumentValue(0, scope));
        });
        hasValue.setReturnable(Returnable.of(Boolean.class));
        hasValue.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            ShadowSection key = keyword.getArguments().get(0);
            return CodeBlock.of("$L.containsValue($L)", input, key.getGeneration(c, type, method)).toString();
        });
        context.addKeyword(hasValue);
    
        SubKeyword size = new SubKeyword("size", new Range.None());
        size.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) Map::size;
        });
        size.setReturnable(Returnable.of(Integer.class));
        size.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return CodeBlock.of("$L.size()", input).toString();
        });
        context.addKeyword(size);
    
        SubKeyword keyset = new SubKeyword("keys", new Range.None());
        keyset.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) Map::keySet;
        });
        keyset.setReturnable(Returnable.of(Set.class));
        keyset.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return CodeBlock.of("$L.keySet()", input).toString();
        });
        context.addKeyword(keyset);
    
        SubKeyword values = new SubKeyword("values", new Range.None());
        values.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) Map::values;
        });
        values.setReturnable(Returnable.of(Set.class));
        values.setCommandGen((input, c, keyword, type, method) -> {
            requireValue(input, keyword);
            return CodeBlock.of("$L.values()", input).toString();
        });
        context.addKeyword(values);
    
    }
    
    @Override
    protected Object onEmptyArguments(Keyword keyword, Stepper stepper, Scope scope) {
        return new HashMap<>();
    }
    
    @Override
    protected Object processArgument(ShadowSection section, int index, Scope scope) {
        if (section instanceof Identifier) {
            return (MapTransformer) input -> Transformer.getter(Map.class, section, scope);
        }
        return super.processArgument(section, index, scope);
    }
    
    @Override
    protected Object onExecute(Keyword keyword, Stepper stepper, Scope scope, List<MapTransformer> input) {
        return useTransform(keyword, input);
    }
    
    @Override
    protected Class<?> getReturnType(List<ShadowSection> args, CompileScope scope) {
        if (args.size() == 0) return Map.class;
        ShadowSection section = args.get(args.size() - 1);
        if (section instanceof Identifier) {
            return TypeChecker.check(scope, section).is(Map.class).orError();
        }
        return super.getReturnType(args, scope);
    }
    
    @Override
    protected String generateNoArgs(GenerateContext context, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method) {
        return CodeBlock.of("new $T<>()", HashMap.class).toString();
    }
    
    @Override
    protected String generateSingle(GenerateContext context, String value, ShadowSection section, TypeSpec.Builder type, MethodSpec.Builder method) {
        if (section instanceof Identifier) {
            TypeChecker.require(context.getScope(), section, Map.class);
            return section.getGeneration(context, type, method);
        }
        return super.generateSingle(context, value, section, type, method);
    }
    
    public interface MapTransformer extends Transformer<Map> {}
    
}
