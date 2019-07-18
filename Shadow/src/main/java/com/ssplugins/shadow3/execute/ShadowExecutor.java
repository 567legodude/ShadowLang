package com.ssplugins.shadow3.execute;

import java.util.Deque;
import java.util.LinkedList;

public class ShadowExecutor {
    
    private Deque<Stepper> steps = new LinkedList<>();
    
    public void add(Stepper stepper) {
        steps.addFirst(stepper);
    }
    
    public void execute() {
        while (steps.size() > 0) {
            Stepper stepper = steps.getFirst();
            stepper.run();
//            if (stepper.isFinished()) steps.removeFirst();
        }
    }

}
