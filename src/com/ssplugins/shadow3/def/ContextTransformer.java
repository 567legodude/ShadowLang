package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.section.Identifier;

public interface ContextTransformer<T extends ShadowEntity> {
    
    ShadowContext get(T t, ShadowContext topContext, ShadowContext currentContext);
    
    static ContextTransformer<Block> blockNone() {
        return (block, topContext, currentContext) -> null;
    }
    
    static ContextTransformer<Keyword> keywordNone() {
        return (keyword, topContext, currentContext) -> null;
    }
    
    static ContextTransformer<Block> blockUse(ShadowContext other) {
        return (block, topContext, currentContext) -> other;
    }
    
    static ContextTransformer<Keyword> keywordUse(ShadowContext other) {
        return (keyword, topContext, currentContext) -> other;
    }
    
    static ContextTransformer<Block> blockModule(int pos) {
        return (block, topContext, currentContext) -> {
            Identifier identifier = block.getIdentifier(pos);
            return currentContext.findModule(identifier.getName()).orElseThrow(ShadowCodeException.noDef(block.getLine(), identifier.getPrimaryToken().getIndex(), "Module not found: " + identifier.getName()));
        };
    }
    
    static ContextTransformer<Keyword> keywordModule(int pos) {
        return (keyword, topContext, currentContext) -> {
            Identifier identifier = keyword.getIdentifier(pos);
            return currentContext.findModule(identifier.getName()).orElseThrow(ShadowCodeException.noDef(keyword.getLine(), identifier.getPrimaryToken().getIndex(), "Module not found: " + identifier.getName()));
        };
    }

}
