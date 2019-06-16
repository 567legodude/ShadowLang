package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.CommandKeyword;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapKeyword extends CommandKeyword<MapKeyword.MapTransformer, Map> {
    
    @SuppressWarnings("unchecked")
    public MapKeyword() {
        super(MapTransformer.class, Map.class, new Range.Any(), "map");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        KeywordType put = new KeywordType("put", new Range.Single(2));
        put.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> {
                List<Object> objects = keyword.argumentValues(scope);
                input.put(objects.get(0), objects.get(1));
                return input;
            };
        });
        context.addKeyword(put);
    
        KeywordType remove = new KeywordType("remove", new Range.Single(1));
        remove.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.remove(keyword.argumentValue(0, scope));
        });
        context.addKeyword(remove);
    
        KeywordType get = new KeywordType("get", new Range.Single(1));
        get.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.get(keyword.argumentValue(0, scope));
        });
        context.addKeyword(get);
    
        KeywordType contains = new KeywordType("contains", new Range.Single(1));
        contains.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.containsKey(keyword.argumentValue(0, scope));
        });
        context.addKeyword(contains);
    
        KeywordType hasValue = new KeywordType("has_value", new Range.Single(1));
        hasValue.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) input -> input.containsValue(keyword.argumentValue(0, scope));
        });
        context.addKeyword(hasValue);
    
        KeywordType size = new KeywordType("size", new Range.None());
        size.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) Map::size;
        });
        context.addKeyword(size);
    
        KeywordType keyset = new KeywordType("keys", new Range.None());
        keyset.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) Map::keySet;
        });
        context.addKeyword(keyset);
    
        KeywordType values = new KeywordType("values", new Range.None());
        values.setAction((keyword, stepper, scope) -> {
            return (MapTransformer) Map::values;
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
    protected Object transformInput(MapTransformer input, Map data) {
        return input.transform(data);
    }
    
    public interface MapTransformer {
        
        Object transform(Map input);
        
    }
    
}
