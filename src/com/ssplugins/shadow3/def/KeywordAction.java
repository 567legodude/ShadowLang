package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;

public interface KeywordAction {
    
    void execute(KeywordType keyword, Stepper stepper, Scope scope);
    
}
