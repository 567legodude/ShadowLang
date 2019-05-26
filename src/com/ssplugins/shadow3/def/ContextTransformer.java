package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;

public interface ContextTransformer<T extends ShadowEntity> {
    
    ShadowContext get(T t, ShadowContext topContext, ShadowContext currentContext);
    
    static ContextTransformer<Block> blockNone() {
        return (block, topContext, currentContext) -> null;
    }
    
    static ContextTransformer<Keyword> keywordNone() {
        return (keyword, topContext, currentContext) -> null;
    }
    
    static ContextTransformer<Block> blockModule(int pos) {
        return (block, topContext, currentContext) -> {
            ShadowSection section = block.getModifiers().get(pos);
            if (!(section instanceof Identifier)) {
                throw new ShadowParseError(section.getLine(), section.getPrimaryToken().getIndex(), "Expecting identifier here.");
            }
            return currentContext.findModule(section.getPrimaryToken().getRaw()).orElseThrow(ShadowException.noDef(block.getLine(), section.getPrimaryToken().getIndex(), "Module not found: " + section.getPrimaryToken().getRaw()));
        };
    }
    
    static ContextTransformer<Keyword> keywordModule(int pos) {
        return (keyword, topContext, currentContext) -> {
            ShadowSection section = keyword.getArguments().get(pos);
            if (!(section instanceof Identifier)) {
                throw new ShadowParseError(section.getLine(), section.getPrimaryToken().getIndex(), "Expecting identifier here.");
            }
            return currentContext.findModule(section.getPrimaryToken().getRaw()).orElseThrow(ShadowException.noDef(keyword.getLine(), section.getPrimaryToken().getIndex(), "Module not found: " + section.getPrimaryToken().getRaw()));
        };
    }

}
