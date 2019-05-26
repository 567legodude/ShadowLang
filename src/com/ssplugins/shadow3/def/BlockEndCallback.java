package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;

public interface BlockEndCallback {
    
    void onEnd(Block block, Stepper stepper, Scope scope);
    
}
