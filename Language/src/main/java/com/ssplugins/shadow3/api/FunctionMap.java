package com.ssplugins.shadow3.api;

import com.ssplugins.shadow3.entity.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FunctionMap {
    
    private Map<Integer, Block> blocks = new HashMap<>();
    
    public boolean contains(int i) {
        return blocks.containsKey(i);
    }
    
    public void set(int i, Block block) {
        blocks.put(i, block);
    }
    
    public Optional<Block> get(int i) {
        return Optional.ofNullable(blocks.get(i));
    }
    
}
