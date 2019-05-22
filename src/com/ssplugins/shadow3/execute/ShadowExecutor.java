package com.ssplugins.shadow3.execute;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Block;

import java.util.Deque;
import java.util.LinkedList;

public class ShadowExecutor {
    
    private Deque<Stepper> steps = new LinkedList<>();
    
    public void add(Stepper stepper) {
        steps.addFirst(stepper);
    }
    
    public void add(Block block) {
        add(block.getStepper());
    }
    
    public void execute(ShadowContext context) {
        execute(new Scope(context));
    }
    
    public void execute(Scope scope) {
        while (steps.size() > 0) {
            Stepper stepper = steps.getFirst();
            stepper.run(this, scope);
            if (stepper.isFinished()) steps.removeFirst();
        }
    }

}
