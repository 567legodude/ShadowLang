package com.ssplugins.shadow3.compile;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.util.CompileScope;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class GenerateContext {
    
    private ShadowContext fullContext;
    private CompileScope compileScope;
    
    private Set<String> identifiers = new HashSet<>();
    
    public GenerateContext(ShadowContext fullContext) {
        this.fullContext = fullContext;
        compileScope = new CompileScope(fullContext);
    }
    
    public void back() {
        compileScope = compileScope.parent();
    }
    
    public void newBlock() {
        compileScope = compileScope.newBlock();
    }
    
    public CompileScope getScope() {
        return compileScope;
    }
    
    public boolean nameExists(String s) {
        return identifiers.contains(s);
    }
    
    public void checkName(String s, Consumer<String> consumer) {
        if (!nameExists(s)) {
            addName(s);
            consumer.accept(s);
        }
    }
    
    public void addName(String s) {
        identifiers.add(s);
    }
    
    public void removeName(String s) {
        identifiers.remove(s);
    }
    
    public String getComponentName(String part) {
        String name = getFullContext().getName();
        if (name == null) throw new ShadowException("Context has no name defined.");
        return name + "_" + part;
    }
    
    public ShadowContext getFullContext() {
        return fullContext;
    }
    
}
