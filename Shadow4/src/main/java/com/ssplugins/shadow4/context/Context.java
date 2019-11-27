package com.ssplugins.shadow4.context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Context {
    
    private File source;
    private String name;
    private String className;
    
    private Context parent;
    
    private Map<String, OperatorMap> operators = new HashMap<>();
    
}
