package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.custom.CommandKeyword;
import com.ssplugins.shadow3.def.custom.Transformer;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListKeyword extends CommandKeyword<List, ListKeyword.ListTransformer> {
    
    @SuppressWarnings("unchecked")
    public ListKeyword() {
        super(List.class, ListTransformer.class, new Range.Any(), "list");
    
        ShadowContext context = new ShadowContext();
        setLookupContext(context);
    
        KeywordType arraylist = new KeywordType("arraylist", new Range.None());
        arraylist.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                return new ArrayList<>();
            };
        });
        context.addKeyword(arraylist);
    
        KeywordType linkedlist = new KeywordType("linkedlist", new Range.None());
        linkedlist.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                return new LinkedList<>();
            };
        });
        context.addKeyword(linkedlist);
    
        KeywordType add = new KeywordType("add", new Range.LowerBound(1));
        add.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.addAll(keyword.argumentValues(scope));
                return input;
            };
        });
        context.addKeyword(add);
    
        KeywordType set = new KeywordType("set", new Range.Single(2));
        set.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.set(keyword.getInt(0, scope), keyword.argumentValue(1, scope));
                return input;
            };
        });
        context.addKeyword(set);
    
        KeywordType remove = new KeywordType("remove", new Range.LowerBound(1));
        remove.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                input.removeAll(keyword.argumentValues(scope));
                return input;
            };
        });
        context.addKeyword(remove);
    
        KeywordType get = new KeywordType("get", new Range.Single(1));
        get.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                Integer i = keyword.getArgument(0, Integer.class, scope, "Argument must be an integer.");
                return input.get(i);
            };
        });
        context.addKeyword(get);
    
        KeywordType contains = new KeywordType("contains", new Range.LowerBound(1));
        contains.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) input -> {
                return input.containsAll(keyword.argumentValues(scope));
            };
        });
        context.addKeyword(contains);
    
        KeywordType size = new KeywordType("size", new Range.None());
        size.setAction((keyword, stepper, scope) -> {
            return (ListTransformer) List::size;
        });
        context.addKeyword(size);
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
    protected Object transformInput(ListTransformer input, List data) {
        return input.transform(data);
    }
    
    public interface ListTransformer extends Transformer<List> {}
    
}
