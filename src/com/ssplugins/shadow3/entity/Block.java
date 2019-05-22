package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenReader;
import com.ssplugins.shadow3.parsing.TokenType;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.LineReader;

import java.util.List;

public class Block extends ShadowEntity {
    
    private String name;
    private List<ShadowSection> modifiers;
    private Identifier[] parameters;
    private ShadowEntity[] contents;
    
    private Stepper stepper;
    
    public Block(Block parent, TokenReader def, LineReader reader) {
        super(def.getLine(), parent);
        name = def.expect(TokenType.IDENTIFIER).getRaw();
        def.setLimit(getLine().getBlockEnd());
        
        BlockType type = reader.getContext().findBlock(name).orElseThrow(ShadowException.noDef(def.getLine(), 0, "No definition found for: " + name));
        type.getReader().accept(this, def);
    
        int params = type.getParameters();
        parameters = new Identifier[params];
        if (def.getIndex() < def.getLimit() || params > 0) {
            def.expect(TokenType.GROUP_OPEN, "(");
            for (int i = 0; i < params; ++i) {
                if (!def.nextMatches(TokenType.IDENTIFIER, null)) {
                    throw new ShadowParseError(def.getLine(), def.peekNext().getIndex(), "Expecting identifier, found: " + def.nextType().name());
                }
                parameters[i] = (Identifier) def.nextSection();
                if (i < params - 1) def.expect(TokenType.OPERATOR, ",");
            }
            def.expect(TokenType.GROUP_CLOSE, ")");
        }
        def.consume();
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        // TODO execute block
        return null;
    }
    
    private Stepper parentStepper() {
        ShadowEntity parent = getParent();
        if (parent == null) return null;
        return ((Block) parent).getStepper();
    }
    
    public String getName() {
        return name;
    }
    
    public ShadowEntity[] getContents() {
        return contents;
    }
    
    public Stepper getStepper() {
        if (stepper == null) stepper = new Stepper(parentStepper(), this);
        return stepper;
    }
    
}
