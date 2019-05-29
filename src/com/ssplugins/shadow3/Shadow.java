package com.ssplugins.shadow3;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.EntityList;
import com.ssplugins.shadow3.entity.ShadowEntity;

import java.util.Optional;
import java.util.stream.Stream;

public class Shadow {
    
    private EntityList contents = new EntityList();
    
    public Stream<ShadowEntity> contentStream() {
        return contents.stream();
    }
    
    public Optional<Block> firstBlock(String name) {
        return contentStream().filter(entity -> entity instanceof Block)
                              .map(e -> (Block) e)
                              .filter(b -> b.getName().equals(name))
                              .findFirst();
    }
    
    public EntityList getContents() {
        return contents;
    }
    
}
