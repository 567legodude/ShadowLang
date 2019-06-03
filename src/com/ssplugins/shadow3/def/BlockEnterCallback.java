package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;

import java.util.Iterator;
import java.util.List;

public interface BlockEnterCallback {
    
    void onEnter(Block block, Stepper stepper, Scope scope, List<Object> args);
    
    static BlockEnterCallback iterateParameter(int i) {
        return (block, stepper, scope, args) -> {
            Iterator<?> it = (Iterator<?>) scope.getBlockValue();
            scope.setLocal(block.getParameters().get(i), it.next());
        };
    }
    
}
