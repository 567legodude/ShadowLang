package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Block;

import java.util.List;

public interface PreRunCheck {
    
    boolean willEnter(Block block, List<Object> args);
    
}
