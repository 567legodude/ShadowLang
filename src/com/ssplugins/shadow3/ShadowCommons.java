package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.OperatorAction;
import com.ssplugins.shadow3.section.Operator.OpOrder;
import com.ssplugins.shadow3.util.Range;

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
        addKeywords();
        addBlocks();
    }
    
    //region Operators
    
    void addOperators() {
        operatorComment();
        operatorBlock();
        operatorAdd();
    }
    
    void operatorComment() {
        context.addOperator(new OperatorAction<>("//", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    void operatorBlock() {
        context.addOperator(new OperatorAction<>("::", OpOrder.ASSIGNMENT, null, null, null, null));
        context.addOperator(new OperatorAction<>("->", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    void operatorAdd() {
        OperatorAction<Integer, Integer, Integer> addInt = new OperatorAction<>("+", int.class, int.class, int.class, Integer::sum);
        context.addOperator(addInt);
        OperatorAction<String, String, String> addString = new OperatorAction<>("+", String.class, String.class, String.class, (a, b) -> a + b);
        context.addOperator(addString);
    }
    
    //endregion
    //region Keywords
    
    void addKeywords() {
        keywordPrint();
    }
    
    void keywordPrint() {
        KeywordType print = new KeywordType("print", new Range.Any());
        print.setAction((keyword, stepper, scope) -> {
            keyword.argumentValues(scope).stream().forEach(System.out::print);
            System.out.println();
            return null;
        });
        context.addKeyword(print);
    }
    
    //endregion
    //region Blocks
    
    void addBlocks() {
        blockMain();
    }
    
    void blockMain() {
        BlockType main = new BlockType("main", new Range.None(), new Range.MinMax(0, 1));
        context.addBlock(main);
    }
    
    //endregion
    
}
