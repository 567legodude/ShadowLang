package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.Returnable;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.NumberType;
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefineBlock extends BlockType {
    
    private static class Self {}
    
    public DefineBlock(String name) {
        super(name, new Range.Of(1, 3), new Range.Any());
    }
    
    public static void getDeclaredType(Block block, ShadowContext context) {
        List<ShadowSection> args = block.getArguments();
        if (args.size() == 1) return;
        Class<?> type = context.getType(block, block.getIdentifier(2).getName());
        block.setDeclaredType(type);
    }
    
    public Optional<Class<?>> getReturnType(Block block, CompileScope scope) {
        if (block.getDefinition() != this) throw new IllegalArgumentException("Block is not a definition block.");
        if (block.isChecked()) {
            if (block.getReturnType() == null) {
                if (block.getDeclaredType() == null) {
//                    throw new ShadowParseError(block.getLine(), block.argumentIndex(-1), "Recursive function detected, cannot infer return type.");
                    return Optional.of(Self.class);
                }
                block.setReturnType(block.getDeclaredType());
            }
            return Optional.of(block.getReturnType());
        }
        block.setChecked(true);
        block.getParameters().forEach(p -> {
            scope.mark(p.getName());
            scope.addCheck(p.getName(), p.getType());
        });
        KeywordType returnDef = block.getInnerContext().findKeyword("return").orElseThrow(() -> new ShadowException("Could not find definition for return keyword."));
        List<Class<?>> types = new ArrayList<>();
        boolean hasValue = addTypes(block, types, returnDef, scope);
        Optional<Class<?>> output;
        if (types.size() == 0 || !hasValue) {
            block.setReturnType(Returnable.empty());
            return Optional.empty();
        }
        if (types.size() == 1) {
            output = Optional.of(types.get(0));
        }
        else {
            output = types.stream().reduce((a, b) -> commonType(a, b, block));
        }
        Class<?> returnType = output.get();
        if (returnType == Self.class) {
            throw new ShadowParseError(block.getLine(), block.argumentIndex(-1), "Recursive function detected, cannot infer return type.");
        }
        block.setReturnType(returnType);
        if (block.getDeclaredType() != null && !NumberType.isAssignableFrom(block.getDeclaredType(), block.getReturnType())) {
            throw new ShadowParseError(block.getLine(), block.argumentIndex(2), "Inferred return type doesn't match declared type.");
        }
        return output;
    }
    
    private boolean addTypes(ShadowEntity entity, List<Class<?>> types, KeywordType def, CompileScope scope) {
        AtomicBoolean hasValue = new AtomicBoolean(true);
        if (entity instanceof Block) {
            CompileScope compileScope = scope.newBlock();
            ((Block) entity).getContents().forEach(e -> {
                if (!addTypes(e, types, def, compileScope)) hasValue.set(false);
            });
        }
        else if (entity instanceof Keyword) {
            Keyword keyword = (Keyword) entity;
            keyword.findReturnType(scope);
            if (keyword.getDefinition() == def) {
                if (keyword.getArguments().size() == 0) hasValue.set(false);
                types.add(keyword.getReturnType());
            }
        }
        return hasValue.get();
    }
    
    private Class<?> commonType(Class<?> a, Class<?> b, Block block) {
        if (a == Self.class) return b;
        if (b == Self.class) return a;
        if (a == b) return a;
        if (NumberType.isAssignableFrom(a, b)) return a;
        if (NumberType.isAssignableFrom(b, a)) return b;
        throw new ShadowParseError(block.getLine(), block.argumentIndex(-1), "Mismatched return types: " + a.getSimpleName() + ", " + b.getSimpleName());
    }
    
    private boolean oneOf(Class<?> a, Class<?> b, Class<?> target) {
        return a == target || b == target;
    }
    
}
