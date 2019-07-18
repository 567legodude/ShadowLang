package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;

import java.util.Iterator;

public interface BlockEndCallback {
    
    void onEnd(Block block, Stepper stepper, Scope scope);
    
    static BlockEndCallback iterateParameter(int i) {
        return (block, stepper, scope) -> {
            Iterator<?> it = (Iterator<?>) scope.getBlockValue();
            if (!it.hasNext()) return;
            scope.setLocal(block.getParameters().get(i), it.next());
            stepper.restart();
        };
    }
    
}
