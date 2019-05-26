package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.OperatorAction;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.Operator.OpOrder;
import com.ssplugins.shadow3.util.Range;

import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

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
        keywordSet();
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
    
    void keywordSet() {
        KeywordType set = new KeywordType("set", new Range.Single(2));
        set.setAction((keyword, stepper, scope) -> {
            Identifier name = keyword.getArgumentSection(0, Identifier.class, "First argument should be identifier.");
            Object o = keyword.argumentValue(1, scope);
            scope.set(name, o);
            return o;
        });
        context.addKeyword(set);
    }
    
    //endregion
    //region Blocks
    
    void addBlocks() {
        blockMain();
        blockRepeat();
    }
    
    void blockMain() {
        BlockType main = new BlockType("main", new Range.None(), new Range.MinMax(0, 1));
        context.addBlock(main);
    }
    
    void blockRepeat() {
        BlockType repeat = new BlockType("repeat", new Range.Single(1), new Range.Single(1));
        repeat.setPreRunCheck((block, scope, args) -> {
            Integer i = block.getArgument(0, Integer.class, scope, "Modifier should be an integer.");
            if (i < 0) throw ShadowException.exec(block, "Repeat count must be positive.").get();
            if (i == 0) return false;
            scope.setBlockValue(IntStream.range(0, i).iterator());
            return true;
        });
        repeat.setEnterCallback((block, stepper, scope, args) -> {
            OfInt it = (OfInt) scope.getBlockValue();
            scope.setLocal(block.getParameters().get(0), it.next());
        });
        repeat.setEndCallback((block, stepper, scope) -> {
            OfInt it = (OfInt) scope.getBlockValue();
            if (!it.hasNext()) return;
            scope.setLocal(block.getParameters().get(0), it.next());
            stepper.restart();
        });
        context.addBlock(repeat);
    }
    
    //endregion
    
}
