package com.ssplugins.shadow3;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.*;
import com.ssplugins.shadow3.def.OperatorType.OperatorMatcher;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.Operator.OpOrder;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

import java.util.List;
import java.util.Objects;
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
        operatorEquals();
        operatorNegate();
        operatorAdd();
        operatorSubtract();
        operatorMultiply();
        operatorDivide();
        operatorExponent();
    }
    
    void operatorComment() {
        context.addOperator(new OperatorType<>("//", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    void operatorBlock() {
        context.addOperator(new OperatorType<>("::", OpOrder.ASSIGNMENT, null, null, null, null));
        context.addOperator(new OperatorType<>("->", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    void operatorEquals() {
        OperatorType<Object, Object, Boolean> equals = new OperatorType<>("==", OpOrder.EQUALITY, Object.class, Object.class, boolean.class, Objects::equals);
        context.addOperator(equals);
        // Covered by above case
//        OperatorType<String, String, Boolean> stringEquals = new OperatorType<>("==", String.class, String.class, boolean.class, String::equals);
//        stringEquals.setMatcher(OperatorMatcher.sameType());
//        context.addOperator(stringEquals);
    }
    
    void operatorNegate() {
        UnaryOperatorType<Integer, Integer> nInt = new UnaryOperatorType<>("-", int.class, int.class, i -> -i);
        context.addOperator(nInt);
        UnaryOperatorType<Double, Double> nDouble = new UnaryOperatorType<>("-", double.class, double.class, d -> -d);
        context.addOperator(nDouble);
        UnaryOperatorType<Long, Long> nLong = new UnaryOperatorType<>("-", long.class, long.class, l -> -l);
        context.addOperator(nLong);
        UnaryOperatorType<Float, Float> nFloat = new UnaryOperatorType<>("-", float.class, float.class, f -> -f);
        context.addOperator(nFloat);
    }
    
    void operatorAdd() {
        OperatorType<String, Object, String> addString = new OperatorType<>("+", String.class, Object.class, String.class, (a, b) -> a + b.toString());
        context.addOperator(addString);
        OperatorType<Object, String, String> addString2 = new OperatorType<>("+", Object.class, String.class, String.class, (a, b) -> a.toString() + b);
        addString2.setMatcher(OperatorMatcher.sameType());
        context.addOperator(addString2);
    
        NumberOperatorType numberAdd = new NumberOperatorType("+", Integer::sum, Double::sum, Float::sum, Long::sum);
        numberAdd.addTo(context);
    }
    
    void operatorSubtract() {
        NumberOperatorType numberSubtract = new NumberOperatorType("-", (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
        numberSubtract.addTo(context);
    }
    
    void operatorMultiply() {
        NumberOperatorType numberMultiply = new NumberOperatorType("*", (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
        numberMultiply.addTo(context);
    }
    
    void operatorDivide() {
        OperatorType<Number, Number, Double> div = new OperatorType<>("/", Number.class, Number.class, double.class, (a, b) -> a.doubleValue() / b.doubleValue());
        context.addOperator(div);
    }
    
    void operatorExponent() {
        OperatorType<Number, Number, Double> exp = new OperatorType<>("^", Number.class, Number.class, double.class, (a, b) -> Math.pow(a.doubleValue(), b.doubleValue()));
        exp.setLeftToRight(false);
        context.addOperator(exp);
    }
    
    // Not added yet
    void operatorBitShift() {
        NumberOperatorType shiftRight = new NumberOperatorType(">>", OpOrder.SHIFT, (a, b) -> a >> b, null, null, (a, b) -> a >> b);
        shiftRight.addTo(context);
        NumberOperatorType shiftLeft = new NumberOperatorType("<<", OpOrder.SHIFT, (a, b) -> a << b, null, null, (a, b) -> a << b);
        shiftLeft.addTo(context);
    }
    
    //endregion
    //region Keywords
    
    void addKeywords() {
        keywordPrint();
        keywordSet();
        keywordType();
        keywordExec();
//        keywordFrom(); Not fully implemented yet
    }
    
    void keywordPrint() {
        KeywordType print = new KeywordType("print", new Range.Any());
        print.setAction((keyword, stepper, scope) -> {
            keyword.argumentValues(scope).forEach(System.out::print);
            System.out.println();
            return null;
        });
        context.addKeyword(print);
    }
    
    void keywordSet() {
        KeywordType set = new KeywordType("set", new Range.Single(2));
        set.setAction((keyword, stepper, scope) -> {
            Identifier name = keyword.getIdentifier(0);
            Object o = keyword.argumentValue(1, scope);
            scope.set(name, o);
            return o;
        });
        context.addKeyword(set);
    }
    
    void keywordType() {
        KeywordType type = new KeywordType("type", new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Object o = keyword.argumentValue(0, scope);
            return o.getClass().getSimpleName();
        });
        context.addKeyword(type);
    }
    
    void keywordExec() {
        KeywordType exec = new KeywordType("exec", new Range.LowerBound(1));
        exec.setAction((keyword, stepper, scope) -> {
            String name = keyword.getIdentifier(0).getName();
            List<Object> params = keyword.argumentValues(scope, 1);
            Block block = scope.getContext().findFunction(name, params.size()).orElseThrow(ShadowException.noDef(keyword.getLine(), keyword.getLine().firstToken().getIndex(), "No matching function found."));
            return block.execute(stepper, new Scope(scope.getContext(), stepper), params);
        });
        context.addKeyword(exec);
    }
    
    void keywordFrom() {
        KeywordType from = new KeywordType("from", new Range.LowerBound(3));
        from.setParseCallback((keyword, c) -> {
            Identifier sep = keyword.getIdentifier(1);
            if (!sep.getName().equals("do")) {
                throw new ShadowParseError(keyword.getLine(), sep.getPrimaryToken().getIndex(), "Expected keyword \"do\" here.");
            }
        });
        from.setContextTransformer(ContextTransformer.keywordModule(0));
        from.setAction((keyword, stepper, scope) -> keyword.argumentValue(2, scope));
        context.addKeyword(from);
    }
    
    //endregion
    //region Blocks
    
    void addBlocks() {
        blockMain();
        blockRepeat();
        blockConditionals();
        blockDefine();
//        blockUsing(); Not fully implemented yet
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
    
    @SuppressWarnings("Duplicates")
    void blockConditionals() {
        BlockType typeIf = new BlockType("if", new Range.Single(1), new Range.None());
        typeIf.setPreRunCheck((block, scope, args) -> {
            return block.getArgument(0, Boolean.class, scope, "Modifier should be a boolean.");
        });
        Schema<ShadowEntity> skipElse = new Schema<>(e -> e.flow().isBlock("elseif", "else"));
        typeIf.setEnterCallback((block, stepper, scope, args) -> {
            stepper.setSkipSchema(skipElse);
        });
    
        Schema<Block> condSchema = new Schema<>(block -> block.flow().prevIsBlock("if", "elseif"));
        condSchema.setSituation("Block must follow an if or elseif block.");
        
        BlockType elseif = new BlockType("elseif", new Range.Single(1), new Range.None());
        elseif.setSchema(condSchema);
        elseif.setPreRunCheck((block, scope, args) -> {
            return block.getArgument(0, Boolean.class, scope, "Modifier should be a boolean.");
        });
        elseif.setEnterCallback((block, stepper, scope, args) -> {
            stepper.setSkipSchema(skipElse);
        });
    
        BlockType typeElse = new BlockType("else", new Range.None(), new Range.None());
        typeElse.setSchema(condSchema);
    
        context.addBlock(typeIf);
        context.addBlock(elseif);
        context.addBlock(typeElse);
    }
    
    void blockDefine() {
        BlockType define = new BlockType("define", new Range.Single(1), new Range.Any());
        define.setParseCallback((block, c) -> c.addFunction(block));
        define.setPreRunCheck((block, scope, args) -> args != null);
        define.setEnterCallback((block, stepper, scope, args) -> {
            List<Identifier> parameters = block.getParameters();
            if (args.size() != parameters.size()) {
                throw new ShadowExecutionError(block.getLine(), block.getLine().firstToken().getIndex(), "Number of arguments does not equal number of parameters.");
            }
            for (int i = 0; i < args.size(); ++i) {
                scope.set(parameters.get(i), args.get(i));
            }
        });
        context.addBlock(define);
    
        ShadowContext defineContext = new ShadowContext();
        define.setContextTransformer((block, topContext, currentContext) -> defineContext);
    
        KeywordType aReturn = new KeywordType("return", new Range.Single(1));
        aReturn.setAction((keyword, stepper, scope) -> {
            Scope original = scope;
            while (stepper.getBlock().getDefinition() != define) {
                stepper.breakBlock();
                stepper = stepper.getParent();
                scope = scope.getParent();
            }
            scope.setReturnValue(keyword.argumentValue(0, original));
            stepper.breakBlock();
            return null;
        });
        defineContext.addKeyword(aReturn);
    }
    
    void blockUsing() {
        BlockType using = new BlockType("using", new Range.Single(1), new Range.None());
//        using.setContextTransformer((block, topContext, currentContext) -> {
//            Identifier module = block.getIdentifier(0);
//            String name = module.getName();
//            return currentContext.findModule(name).orElseThrow(ShadowException.noDef(module.getLine(), module.getPrimaryToken().getIndex(), "No module found named: " + name));
//        });
        using.setContextTransformer(ContextTransformer.blockModule(0));
        context.addBlock(using);
    }
    
    //endregion
    
}
