package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.entity.Block;

import java.util.Optional;

public interface FunctionSearch {
    
    Optional<Block> find(String name, CompileScope scope, int size);
    
}
