package com.ssplugins.shadow3;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.JavaGen;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.EntityList;
import com.ssplugins.shadow3.entity.ShadowEntity;

import java.util.Optional;
import java.util.stream.Stream;

public class Shadow {
    
    private ShadowContext context;
    private EntityList contents = new EntityList();
    
    public Shadow(ShadowContext context) {
        this.context = context;
    }
    
    public Stream<ShadowEntity> contentStream() {
        return contents.stream();
    }
    
    public Optional<Block> firstBlock(String name) {
        return contentStream().filter(entity -> entity instanceof Block)
                              .map(e -> (Block) e)
                              .filter(b -> b.getName().equals(name))
                              .findFirst();
    }
    
    public TypeSpec generateCode(String name) {
        context.setFileName(name);
        TypeSpec.Builder builder = TypeSpec.classBuilder(name);
        contentStream().filter(entity -> entity instanceof Block)
                       .map(entity -> (Block) entity)
                       .forEach(block -> block.generateCode(builder, null));
        return builder.build();
    }
    
    public JavaFile generateFile() {
        String name = context.getSource().getName();
        name = name.substring(0, name.lastIndexOf('.'));
        name = JavaGen.checkName(name.replace('.', '_'));
        return JavaFile.builder("", generateCode(name)).build();
    }
    
    public EntityList getContents() {
        return contents;
    }
    
}
