package com.ssplugins.shadow3.execute;

import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.ShadowEntity;

import java.util.List;

public class Stepper {
    
    private ShadowEntity[] content;
    
    private Stepper parent;
    private boolean run;
    private int index;
    
    public Stepper(ShadowEntity content, Stepper parent) {
        this(parent, new ShadowEntity[] {content});
    }
    
    public Stepper(Stepper parent, ShadowEntity[] content) {
        this.parent = parent;
        this.content = content;
    }
    
    public Stepper(Stepper parent, Block block) {
        this(parent, block.getContents());
    }
    
    public boolean isFinished() {
        return index == content.length;
    }
    
    public void run(Scope scope, List<Object> args) {
        run = true;
        while (run && index < content.length) {
            content[index].execute(this, scope, args);
            ++index;
        }
    }
    
    public void breakBlock() {
        run = false;
    }
    
    public Stepper getParent() {
        return parent;
    }
    
}
