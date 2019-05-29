package com.ssplugins.shadow3.def;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.ShadowEntity;

public interface ParseCallback<T extends ShadowEntity> {
    
    void onParse(T t, ShadowContext context);
    
}
