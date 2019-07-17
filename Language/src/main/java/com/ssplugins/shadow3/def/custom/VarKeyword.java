package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.commons.ShadowCommons;
import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.util.Range;

public class VarKeyword<T> extends KeywordType {
    
    private BlockType def;
    private Class<T> type;
    private VarKeywordAction<T> action;
    
    public VarKeyword(String name, Range arguments, BlockType def, Class<T> type) {
        super(name, arguments);
        this.def = def;
        this.type = type;
        super.setAction((keyword, stepper, scope) -> {
            Stepper s = ShadowCommons.findStepper(stepper, def, keyword, null);
            if (s == null) {
                throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(-1), "Cannot find corresponding block for keyword.");
            }
            Var<T> v = new Var<T>() {
                @Override
                public T get() {
                    Object value = stepper.getScope().getBlockValue();
                    if (!type.isInstance(value)) {
                        throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(-1), "Incompatible block value.");
                    }
                    return type.cast(value);
                }
    
                @Override
                public void set(T t) {
                    stepper.getScope().setBlockValue(t);
                }
            };
            if (action != null) return action.execute(keyword, stepper, scope, v);
            return null;
        });
    }
    
    public void setAction(VarKeywordAction<T> action) {
        this.action = action;
    }
    
    public interface VarKeywordAction<U> {
        
        Object execute(Keyword keyword, Stepper stepper, Scope scope, Var<U> var);
        
    }
    
    public interface Var<U> {
        
        U get();
    
        void set(U u);
        
    }
    
}
