package com.ssplugins.shadow3.section;

import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.util.OperatorTree;

import java.util.List;
import java.util.stream.Stream;

public class Compound extends ShadowSection {
    
    private OperatorTree opTree;
    
    private boolean constant = true;
    private boolean computed = false;
    private Object value;
    
    public Compound(TokenLine line, List<ShadowSection> sections, ShadowEntity parent) {
        super(line);
        this.setTokens(sections.stream().map(ShadowSection::getTokens).flatMap(Stream::of).toArray(Token[]::new));
        
        opTree = new OperatorTree(line);
        for (ShadowSection section : sections) {
            // If this compound is not a constant value, the variable is set to false and stays false.
            constant &= (section instanceof Operator || section instanceof ShadowString || section instanceof ShadowNumber);
            if (section instanceof Operator) {
                Operator operator = (Operator) section;
                if (opTree.expectingUnary()) {
                    operator.lookupUnary(parent);
                    opTree.insert(new OperatorTree.UnaryOpNode(operator), section.getPrimaryToken());
                }
                else {
                    operator.lookup(parent);
                    opTree.insert(new OperatorTree.OpNode(operator), section.getPrimaryToken());
                }
            }
            else {
                opTree.insert(new OperatorTree.SectionNode(section), section.getPrimaryToken());
            }
        }
        if (!opTree.isFinished()) {
            throw new ShadowParseError(line, line.lastToken().getIndex(), "Incomplete expression.");
        }
    }
    
    @Override
    public Object toObject(Scope scope) {
        if (computed) return value;
        if (constant) {
            value = opTree.getValue(scope);
            computed = true;
            return value;
        }
        return opTree.getValue(scope);
    }
    
    public OperatorTree getOpTree() {
        return opTree;
    }
    
}
