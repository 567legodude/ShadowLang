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
        operatorBlock();
        operatorAdd();
    }
    
    void operatorBlock() {
        context.addOperator(new OperatorAction<>("::", OpOrder.ASSIGNMENT, null, null, null, null));
        context.addOperator(new OperatorAction<>("->", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    void operatorAdd() {
        OperatorAction<Integer, Integer, Integer> addInt = new OperatorAction<>("+", int.class, int.class, int.class, Integer::sum);
        context.addOperator(addInt);
        OperatorAction<String, Object, String> addString = new OperatorAction<>("+", String.class, Object.class, String.class, (s, o) -> s + o.toString());
        context.addOperator(addString);
    }
    
    //endregion
    //region Keywords
    
    void addKeywords() {
        keywordExpression();
        keywordPrint();
    }
    
    void keywordPrint() {
        KeywordType print = new KeywordType("print", new Range.Any());
        context.addKeyword(print);
    }
    
    void keywordExpression() {
        KeywordType expr = new KeywordType(":", new Range.Any());
        context.addKeyword(expr);
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
