package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.commons.ShadowPredicate;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.util.Range;

import java.util.function.BiFunction;

public class SimplePredicate<I, C> extends KeywordType {
    
    public SimplePredicate(String name) {
        super(name, new Range.Single(1));
    }
    
    public void setTest(Class<I> inputType, Class<C> compareType, BiFunction<I, C, Boolean> function) {
        super.setAction((keyword, stepper, scope) -> {
            C arg = keyword.getArgument(0, compareType, scope, "Incorrect argument type for this keyword.");
            return new ShadowPredicate(keyword.getLine(), keyword.argumentIndex(-1))
                    .validate(ShadowPredicate.match(1, inputType))
                    .test(ShadowPredicate.as(inputType, i -> function.apply(i, arg)));
        });
    }
    
}
