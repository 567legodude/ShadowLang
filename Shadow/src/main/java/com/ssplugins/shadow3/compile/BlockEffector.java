package com.ssplugins.shadow3.compile;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.util.CompileScope;

public interface BlockEffector {
    
    void apply(Block block, CompileScope scope);
    
}
