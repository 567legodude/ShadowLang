package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.execute.Scope;

import java.util.List;

public interface PreRunCheck {
    
    boolean willEnter(Block block, Scope scope, List<Object> args);
    
    
    
}
