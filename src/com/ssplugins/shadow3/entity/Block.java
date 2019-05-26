package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.api.ShadowContext;
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
import com.ssplugins.shadow3.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Block extends ShadowEntity {
    
    private String name;
    private List<ShadowSection> modifiers;
    private List<Identifier> parameters;
    private EntityList contents;
    
    private BlockType definition;
    private ShadowContext innerContext;
    
    public Block(Block parent, LineReader reader) {
        super(reader.next(), parent);
        TokenReader def = new TokenReader(this, reader.getParser(), getLine());
        name = def.expect(TokenType.IDENTIFIER).getRaw();
    
        definition = findDef(parent, reader.getContext());
        
        def.setLimit(getLine().getBlockEnd());
        modifiers = new ArrayList<>();
        while (def.hasNext() && !def.nextMatches(TokenType.OPERATOR, "->")) {
            modifiers.add(def.nextSection());
        }
    
        parameters = new ArrayList<>();
        if (def.nextMatches(TokenType.OPERATOR, "->")) {
            parameters.add(def.expectSection(Identifier.class, "identifier"));
            while (def.hasNext()) {
                def.expect(TokenType.OPERATOR, ",");
                parameters.add(def.expectSection(Identifier.class, "identifier"));
            }
        }
        def.reset();
    
        Range mods = definition.getModifiers();
        if (!mods.contains(modifiers.size())) {
            throw new ShadowParseError(getLine(), getLine().firstToken().getIndex(), "Block expects " + mods.toString("modifier") + ", found " + modifiers.size());
        }
        Range params = definition.getParameters();
        if (!params.contains(parameters.size())) {
            throw new ShadowParseError(getLine(), getLine().firstToken().getIndex(), "Block expects " + params.toString("parameter") + ", found " + parameters.size());
        }
    
        innerContext = definition.getContextTransformer().get(this, reader.getContext(), (parent == null ? reader.getContext() : parent.getInnerContext()));
        
        contents = new EntityList();
        if (def.hasNext() && def.nextMatches(TokenType.OPERATOR, "::")) {
            def.consume();
            Keyword keyword = reader.getParser().readKeyword(this, def, reader.getContext());
            contents.add(keyword);
        }
        else {
            def.expect(TokenType.GROUP_OPEN, "{");
            while (!reader.nextIsClose()) {
                contents.add(reader.nextEntity(this, ShadowException.noClose(getLine(), getLine().lastToken().getIndex(), "Reached end of file while searching for closing bracket.")));
            }
            reader.consume();
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object execute(Stepper stepper, Scope scope, List<Object> args) {
        // TODO execute block
        return null;
    }
    
    @Override
    public void addArgument(ShadowSection section) {
        modifiers.add(section);
    }
    
    @Override
    public ShadowContext getInnerContext() {
        return innerContext;
    }
    
    private BlockType findDef(Block parent, ShadowContext fallback) {
        while (parent != null) {
            ShadowContext context = parent.getInnerContext();
            Optional<BlockType> block = context.findBlock(name);
            if (block.isPresent()) return block.get();
            parent = (Block) parent.getParent();
        }
        return fallback.findBlock(name).orElseThrow(ShadowException.noDef(getLine(), getLine().firstToken().getIndex(), "No definition found for block: " + name));
    }
    
    public List<ShadowSection> getModifiers() {
        return modifiers;
    }
    
    public List<Identifier> getParameters() {
        return parameters;
    }
    
    public EntityList getContents() {
        return contents;
    }
    
    public BlockType getDefinition() {
        return definition;
    }
    
}
