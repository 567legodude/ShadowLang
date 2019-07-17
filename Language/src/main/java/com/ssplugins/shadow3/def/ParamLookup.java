package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.exception.ShadowException;

public interface ParamLookup {
    
    Class<?> getParamType(int index, Block block);
    
    static ParamLookup constant(Class<?> type) {
        return (index, block) -> type;
    }
    
    static ParamLookup of(Class<?>... types) {
        return (index, block) -> {
            if (index >= types.length) throw new ShadowException("Invalid param index: " + index);
            return types[index];
        };
    }
    
}
