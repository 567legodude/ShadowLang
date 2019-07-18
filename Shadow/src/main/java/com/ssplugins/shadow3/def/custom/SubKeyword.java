package com.ssplugins.shadow3.def.custom;

import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.util.Range;

public class SubKeyword extends KeywordType {
    
    private CommandGen generator;
    
    public SubKeyword(String name, Range arguments) {
        super(name, arguments);
    }
    
    public CommandGen getCommandGen() {
        return generator;
    }
    
    public void setCommandGen(CommandGen generator) {
        this.generator = generator;
    }
    
}
