package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;

public interface KeywordAction {
    
    Object execute(Keyword keyword, Stepper stepper, Scope scope);
    
}
