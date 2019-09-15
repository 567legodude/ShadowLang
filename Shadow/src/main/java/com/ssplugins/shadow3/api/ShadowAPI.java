package com.ssplugins.shadow3.api;

import com.ssplugins.shadow3.exception.ShadowException;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ShadowAPI {
    
    public abstract void loadInto(ShadowContext context);
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    protected @interface Entity {}
    
    protected void callAnnotatedMethods() {
        try {
            for (Method method : this.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Entity.class)) continue;
                if (method.getParameterCount() != 0) continue;
                method.setAccessible(true);
                method.invoke(this);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ShadowException(e);
        }
    }

}
