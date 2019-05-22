package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.OperatorAction;
import com.ssplugins.shadow3.section.Operator.OpOrder;

@SuppressWarnings("WeakerAccess")
public class ShadowCommons extends ShadowAPI {
    
    public static ShadowContext create() {
        ShadowContext context = new ShadowContext();
        new ShadowCommons().load(context);
        return context;
    }
    
    private ShadowContext context;
    
    @Override
    public void load(ShadowContext context) {
        this.context = context;
        addOperators();
    }
    
    void addOperators() {
        operatorBlock();
        operatorAdd();
    }
    
    void operatorBlock() {
        context.addOperator(new OperatorAction<>("::", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    void operatorAdd() {
        OperatorAction<Integer, Integer, Integer> addInt = new OperatorAction<>("+", int.class, int.class, int.class, Integer::sum);
        context.addOperator(addInt);
        OperatorAction<String, Object, String> addString = new OperatorAction<>("+", String.class, Object.class, String.class, (s, o) -> s + o.toString());
        context.addOperator(addString);
    }
    
}
