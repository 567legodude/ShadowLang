package com.ssplugins.shadow3.execute;

import com.ssplugins.shadow3.entity.ShadowEntity;

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
    
    public boolean isFinished() {
        return index == content.length;
    }
    
    public void run(Scope scope) {
        run = true;
        while (run && index < content.length) {
            content[index].execute(this, scope, null);
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
