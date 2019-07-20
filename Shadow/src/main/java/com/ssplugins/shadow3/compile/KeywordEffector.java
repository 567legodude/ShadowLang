package com.ssplugins.shadow3.compile;

import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.util.CompileScope;

public interface KeywordEffector {
    
    void apply(Keyword keyword, CompileScope scope);
    
}
