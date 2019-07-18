package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ssplugins.shadow3.def.OperatorType.OperatorMatcher.numberType;

public class DefineBlock extends BlockType {
    
    public DefineBlock() {
        super("define", new Range.Single(1), new Range.Any());
    }
    
    public Optional<Class<?>> getReturnType(Block block, CompileScope scope) {
        if (block.getDefinition() != this) throw new IllegalArgumentException("Block is not a definition block.");
        KeywordType returnDef = block.getInnerContext().findKeyword("return").orElseThrow(() -> new ShadowException("Could not find definition for return keyword."));
        List<Class<?>> types = new ArrayList<>();
        boolean hasValue = addTypes(block, types, returnDef, scope);
        if (types.size() == 0 || !hasValue) return Optional.empty();
        if (types.size() == 1) return Optional.of(types.get(0));
        return types.stream().reduce((a, b) -> commonType(a, b, block));
    }
    
    private boolean addTypes(ShadowEntity entity, List<Class<?>> types, KeywordType def, CompileScope scope) {
        AtomicBoolean hasValue = new AtomicBoolean(true);
        if (entity instanceof Block) ((Block) entity).getContents().forEach(e -> {
            if (!addTypes(e, types, def, scope)) hasValue.set(false);
        });
        else if (entity instanceof Keyword) {
            Keyword keyword = (Keyword) entity;
            if (keyword.getDefinition() == def) {
                if (keyword.getArguments().size() == 0) return false;
                types.add(def.getReturnable().getReturnType((Keyword) entity, scope));
            }
        }
        return hasValue.get();
    }
    
    private Class<?> commonType(Class<?> a, Class<?> b, Block block) {
        if (a == b) return a;
        if (a.isAssignableFrom(b)) return a;
        if (b.isAssignableFrom(a)) return b;
        if (numberType(a) && numberType(b)) {
            if (oneOf(a, b, Double.class)) return Double.class;
            else if (oneOf(a, b, Float.class)) return Float.class;
            else if (oneOf(a, b, Long.class)) return Long.class;
            else if (oneOf(a, b, Integer.class)) return Integer.class;
            else if (oneOf(a, b, Short.class)) return Short.class;
            else return Byte.class;
        }
        throw new ShadowParseError(block.getLine(), block.argumentIndex(-1), "Mismatched return types: " + a.getSimpleName() + ", " + b.getSimpleName());
    }
    
    private boolean oneOf(Class<?> a, Class<?> b, Class<?> target) {
        return a == target || b == target;
    }
    
}
