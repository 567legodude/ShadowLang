package com.ssplugins.shadow3.commons;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.*;
import com.ssplugins.shadow3.def.OperatorType.OperatorMatcher;
import com.ssplugins.shadow3.def.custom.NumberCompareOp;
import com.ssplugins.shadow3.def.custom.NumberOperatorType;
import com.ssplugins.shadow3.def.custom.StringPredicate;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.Operator.OpOrder;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.Range;
import com.ssplugins.shadow3.util.Schema;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShadowCommons extends ShadowAPI {
    
    public static ShadowContext create(File file) {
        ShadowContext context = new ShadowContext(file);
        new ShadowCommons().loadInto(context);
        return context;
    }
    
    private ShadowContext context;
    
    private static void argsToParams(Block block, Stepper stepper, Scope scope, List<Object> args) {
        List<Identifier> parameters = block.getParameters();
        if (args.size() != parameters.size()) {
            throw new ShadowExecutionError(block.getLine(), block.argumentIndex(-1), "Number of arguments does not equal number of parameters.");
        }
        for (int i = 0; i < args.size(); ++i) {
            scope.set(parameters.get(i), args.get(i));
        }
    }
    
    private static KeywordAction returnArgument(BlockType def) {
        return (keyword, stepper, scope) -> {
            Scope original = scope;
            while (stepper.getBlock().getDefinition() != def) {
                stepper.breakBlock();
                stepper = stepper.getParent();
                scope = scope.getParent();
            }
            scope.setReturnValue(keyword.argumentValue(0, original));
            stepper.breakBlock();
            return null;
        };
    }
    
    @Override
    public void loadInto(ShadowContext context) throws ShadowException {
        this.context = context;
        this.callAnnotatedMethods();
        this.context = null;
    }
    
    //region Operators
    
    @Entity
    void operatorComment() {
        context.addOperator(new OperatorType<>("//", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    @Entity
    void operatorBlock() {
        context.addOperator(new OperatorType<>("::", OpOrder.ASSIGNMENT, null, null, null, null));
        context.addOperator(new OperatorType<>("->", OpOrder.ASSIGNMENT, null, null, null, null));
    }
    
    @Entity
    void operatorParam() {
        OperatorType<Object, Object, Parameters> param = new OperatorType<>(",", OpOrder.COMMA, Object.class, Object.class, Parameters.class, (o, o2) -> {
            if (o instanceof Parameters && o2 instanceof Parameters) {
                Parameters p = (Parameters) o;
                p.getParams().addAll(((Parameters) o2).getParams());
                return p;
            }
            else if (o instanceof Parameters) {
                Parameters p = (Parameters) o;
                p.addParam(o2);
                return p;
            }
            else if (o2 instanceof Parameters) {
                Parameters p = (Parameters) o2;
                p.getParams().add(0, o);
                return p;
            }
            Parameters p = new Parameters();
            p.addParam(o);
            p.addParam(o2);
            return p;
        });
        context.addOperator(param);
    }
    
    @Entity
    void operatorInput() {
        OperatorType<Object, ShadowPredicate, Boolean> input = new OperatorType<>("=>", OpOrder.INPUT, Object.class, ShadowPredicate.class, boolean.class, (o, predicate) -> {
            Parameters params;
            if (o instanceof Parameters) params = (Parameters) o;
            else {
                params = new Parameters();
                params.addParam(o);
            }
            return predicate.get().test(params);
        });
        context.addOperator(input);
    }
    
    @Entity
    void operatorAssignment() {
        OperatorType<Integer, Object, IndexAssignment> intAssign = new OperatorType<>("=", OpOrder.ASSIGNMENT, Integer.class, Object.class, IndexAssignment.class, IndexAssignment::new);
        context.addOperator(intAssign);
    }
    
    @Entity
    void operatorEquals() {
        OperatorType<Object, Object, Boolean> equals = new OperatorType<>("==", OpOrder.EQUALITY, Object.class, Object.class, boolean.class, Objects::equals);
        context.addOperator(equals);
    }
    
    @Entity
    void operatorNotEqual() {
        OperatorType<Object, Object, Boolean> notEqual = new OperatorType<>("!=", OpOrder.EQUALITY, Object.class, Object.class, boolean.class, (o, o2) -> o != o2);
        context.addOperator(notEqual);
    }
    
    @Entity
    void operatorNot() {
        UnaryOperatorType<Boolean, Boolean> not = new UnaryOperatorType<>("!", boolean.class, boolean.class, b -> !b);
        context.addOperator(not);
    }
    
    @Entity
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
    
    @Entity
    void operatorAdd() {
        OperatorType<String, Object, String> addString = new OperatorType<>("+", String.class, Object.class, String.class, (a, b) -> a + b.toString());
        context.addOperator(addString);
        OperatorType<Object, String, String> addString2 = new OperatorType<>("+", Object.class, String.class, String.class, (a, b) -> a.toString() + b);
        addString2.setMatcher(OperatorMatcher.sameType());
        context.addOperator(addString2);
    
        NumberOperatorType numberAdd = new NumberOperatorType("+", Integer::sum, Double::sum, Float::sum, Long::sum);
        numberAdd.addTo(context);
    }
    
    @Entity
    void operatorSubtract() {
        NumberOperatorType numberSubtract = new NumberOperatorType("-", (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
        numberSubtract.addTo(context);
    }
    
    @Entity
    void operatorMultiply() {
        NumberOperatorType numberMultiply = new NumberOperatorType("*", (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
        numberMultiply.addTo(context);
    }
    
    @Entity
    void operatorDivide() {
        OperatorType<Number, Number, Double> div = new OperatorType<>("/", Number.class, Number.class, double.class, (a, b) -> a.doubleValue() / b.doubleValue());
        context.addOperator(div);
    }
    
    @Entity
    void operatorExponent() {
        OperatorType<Number, Number, Double> exp = new OperatorType<>("^", Number.class, Number.class, double.class, (a, b) -> Math.pow(a.doubleValue(), b.doubleValue()));
        exp.setLeftToRight(false);
        context.addOperator(exp);
    }
    
    @Entity
    void operatorModulus() {
        OperatorType<Number, Number, Double> modulus = new OperatorType<>("%", OpOrder.MUL_DIV, Number.class, Number.class, double.class, (a, b) -> a.doubleValue() % b.doubleValue());
        context.addOperator(modulus);
    }
    
    @Entity
    void operatorCompare() {
        NumberCompareOp lt = new NumberCompareOp("<", OpOrder.COMPARE, (a, b) -> a < b, (a, b) -> a < b, (a, b) -> a < b, (a, b) -> a < b);
        lt.addTo(context);
        NumberCompareOp lte = new NumberCompareOp("<=", OpOrder.COMPARE, (a, b) -> a <= b, (a, b) -> a <= b, (a, b) -> a <= b, (a, b) -> a <= b);
        lte.addTo(context);
        NumberCompareOp gt = new NumberCompareOp(">", OpOrder.COMPARE, (a, b) -> a > b, (a, b) -> a > b, (a, b) -> a > b, (a, b) -> a > b);
        gt.addTo(context);
        NumberCompareOp gte = new NumberCompareOp(">=", OpOrder.COMPARE, (a, b) -> a >= b, (a, b) -> a >= b, (a, b) -> a >= b, (a, b) -> a >= b);
        gte.addTo(context);
    }
    
    @Entity
    void operatorBoolean() {
        OperatorType<Boolean, Boolean, Boolean> and = new OperatorType<>("&&", OpOrder.AND, boolean.class, boolean.class, boolean.class, (a, b) -> a && b);
        context.addOperator(and);
        OperatorType<Boolean, Boolean, Boolean> or = new OperatorType<>("||", OpOrder.OR, boolean.class, boolean.class, boolean.class, (a, b) -> a || b);
        context.addOperator(or);
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
    
    private final Schema<Keyword> INLINE_ONLY = Keyword.inlineOnly();
    
    @Entity
    void keywordExit() {
        KeywordType exit = new KeywordType("exit", new Range.None());
        exit.setAction((keyword, stepper, scope) -> {
            System.exit(0);
            return null;
        });
        context.addKeyword(exit);
    }
    
    @Entity
    void keywordPrint() {
        KeywordType print = new KeywordType("print", new Range.Any());
        print.setAction((keyword, stepper, scope) -> {
            keyword.argumentValues(scope).forEach(System.out::print);
            System.out.println();
            return null;
        });
        context.addKeyword(print);
    }
    
    @Entity
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
    
    @Entity
    void keywordType() {
        KeywordType type = new KeywordType("type", new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Object o = keyword.argumentValue(0, scope);
            return o.getClass().getSimpleName();
        });
        context.addKeyword(type);
    }
    
    @Entity
    void keywordExec() {
//        KeywordType exec = new KeywordType("exec", new Range.LowerBound(1));
//        exec.setAction((keyword, stepper, scope) -> {
//            String name = keyword.getIdentifier(0).getName();
//            List<Object> params = keyword.argumentValues(scope, 1);
//            Block block = scope.getContext().findFunction(name, params.size()).orElseThrow(ShadowCodeException.noDef(keyword.getLine(), keyword.getLine().firstToken().getIndex(), "No matching function found."));
//            return block.execute(stepper, new Scope(scope.getContext(), stepper), params);
//        });
        KeywordType exec = new KeywordType("exec", new Range.MinMax(1, 2));
        exec.setAction((keyword, stepper, scope) -> {
            String name = keyword.getIdentifier(0).getName();
            List<Object> params;
            if (keyword.getArguments().size() == 1) params = Collections.emptyList();
            else {
                Object o = keyword.argumentValue(1, scope);
                if (o instanceof Parameters) params = ((Parameters) o).getParams();
                else params = Collections.singletonList(o);
            }
            Block block = scope.getContext().findFunction(name, params.size()).orElseThrow(ShadowCodeException.noDef(keyword.getLine(), keyword.argumentIndex(0), "No matching function found."));
            return block.execute(stepper, new Scope(scope.getContext(), stepper), params);
        });
        context.addKeyword(exec);
    }
    
    @Entity
    void keywordChars() {
        KeywordType chars = new KeywordType("chars", new Range.Single(1));
        chars.setAction((keyword, stepper, scope) -> {
            Object o = keyword.argumentValue(0, scope);
            if (!(o instanceof String)) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(0), "First argument must be a string.");
            }
            return ((String) o).chars().mapToObj(i -> String.valueOf((char) i)).iterator();
        });
        context.addKeyword(chars);
    }
    
    @Entity
    void keywordCount() {
        KeywordType count = new KeywordType("count", new Range.MinMax(1, 3));
        count.setAction((keyword, stepper, scope) -> {
            int args = keyword.getArguments().size();
            Integer start = keyword.getArgument(0, Integer.class, scope, "First argument must be an integer.");
            if (args == 1) return IntStream.range(0, start).iterator();
            Integer stop = keyword.getArgument(1, Integer.class, scope, "Second argument must be an integer.");
            if (args == 2) return IntStream.range(start, stop).iterator();
            Integer step = keyword.getArgument(2, Integer.class, scope, "Third argument must be an integer.");
            return new Iterator<Integer>() {
                private int value = start;
                
                @Override
                public boolean hasNext() {
                    return value < stop;
                }
    
                @Override
                public Integer next() {
                    int r = value;
                    value += step;
                    return r;
                }
            };
        });
        context.addKeyword(count);
    }
    
    @Entity
    void keywordList() {
        ListKeyword listKeyword = new ListKeyword();
        context.addKeyword(listKeyword);
    }
    
    @Entity
    void keywordMap() {
        MapKeyword mapKeyword = new MapKeyword();
        context.addKeyword(mapKeyword);
    }
    
    @Entity
    void keywordArray() {
        ArrayKeyword array = new ArrayKeyword();
        this.context.addKeyword(array);
    }
    
    @Entity
    void keywordLen() {
        KeywordType len = new KeywordType("len", new Range.Single(1));
        len.setAction((keyword, stepper, scope) -> {
            Object o = keyword.argumentValue(0, scope);
            if (o instanceof String) return ((String) o).length();
            else if (o.getClass().isArray()) return Array.getLength(o);
            else if (o instanceof Collection) return ((Collection) o).size();
            else if (o instanceof Map) return ((Map) o).size();
            throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(0), "Argument must be a string, array, or collection.");
        });
        context.addKeyword(len);
    }
    
    @Entity
    void keywordSleep() {
        KeywordType sleep = new KeywordType("sleep", new Range.Single(1));
        sleep.setAction((keyword, stepper, scope) -> {
            Number n = keyword.getArgument(0, Number.class, scope, "First argument must be a number");
            long time = n.longValue();
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Sleep interrupted.");
            }
            return null;
        });
        context.addKeyword(sleep);
    }
    
    @Entity
    void keywordInput() {
        KeywordType input = new KeywordType("input", new Range.MinMax(0, 1));
        input.setAction((keyword, stepper, scope) -> {
            if (keyword.getArguments().size() == 1) {
                System.out.print(keyword.argumentValue(0, scope));
            }
            StringBuilder builder = new StringBuilder();
            try {
                char c;
                while ((c = (char) System.in.read()) != '\n') {
                    builder.append(c);
                }
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return builder.toString();
        });
        context.addKeyword(input);
    }
    
    @Entity
    void keywordRandom() {
        KeywordType random = new KeywordType("random", new Range.MinMax(0, 2));
        random.setAction((keyword, stepper, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 0) return ThreadLocalRandom.current().nextDouble();
            if (args.size() == 1) {
                Integer bound = keyword.getArgument(0, Integer.class, scope, "Argument must be an integer");
                return ThreadLocalRandom.current().nextInt(bound);
            }
            Integer lower = keyword.getArgument(0, Integer.class, scope, "Argument must be an integer.");
            Integer upper = keyword.getArgument(1, Integer.class, scope, "Argument must be an integer.");
            return ThreadLocalRandom.current().nextInt(lower, upper);
        });
        context.addKeyword(random);
    }
    
    @Entity
    void keywordTry() {
        KeywordType aTry = new KeywordType("try", new Range.Single(3));
        aTry.setParseCallback((keyword, context1) -> {
            Identifier id = keyword.getIdentifier(1);
            if (!id.getName().equals("or")) {
                throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(1), "Expected \"or\" here.");
            }
        });
        aTry.setAction((keyword, stepper, scope) -> {
            try {
                return keyword.argumentValue(0, scope);
            } catch (Exception e) {
                return keyword.argumentValue(2, scope);
            }
        });
        context.addKeyword(aTry);
    }
    
    //region Predicates
    
    @Entity
    void keywordStartsWith() {
        StringPredicate startsWith = new StringPredicate("starts_with");
        startsWith.setTest(String::startsWith);
        context.addKeyword(startsWith);
    }
    
    @Entity
    void keywordEndsWith() {
        StringPredicate endsWith = new StringPredicate("ends_with");
        endsWith.setTest(String::endsWith);
        context.addKeyword(endsWith);
    }
    
    @Entity
    void keywordContains() {
        StringPredicate contains = new StringPredicate("contains");
        contains.setTest(String::contains);
        context.addKeyword(contains);
    }
    
    @Entity
    void keywordEmpty() {
        KeywordType empty = new KeywordType("empty", new Range.Single(1));
        empty.setAction((keyword, stepper, scope) -> {
            String argument = keyword.getArgument(0, String.class, scope, "Argument must be a string.");
            return argument.isEmpty();
        });
        context.addKeyword(empty);
    }
    
    //endregion
    
    //region Conversions
    
    @Entity
    void keywordStr() {
        KeywordType str = new KeywordType("str", new Range.Single(1));
        str.setAction((keyword, stepper, scope) -> keyword.argumentValue(0, scope).toString());
        context.addKeyword(str);
    }
    
    @Entity
    void keywordInt() {
        KeywordType anInt = new KeywordType("int", new Range.MinMax(1, 2));
        anInt.setAction((keyword, stepper, scope) -> {
            String argument = keyword.getArgument(0, String.class, scope, "Argument must be a string.");
            if (keyword.getArguments().size() == 1) {
                return Integer.parseInt(argument);
            }
            else {
                Integer radix = keyword.getArgument(1, Integer.class, scope, "Argument must be an integer.");
                return Integer.parseInt(argument, radix);
            }
        });
        context.addKeyword(anInt);
    }
    
    @Entity
    void keywordDouble() {
        KeywordType aDouble = new KeywordType("double", new Range.MinMax(1, 2));
        aDouble.setAction((keyword, stepper, scope) -> Double.parseDouble(keyword.getArgument(0, String.class, scope, "Argument must be a string.")));
        context.addKeyword(aDouble);
    }
    
    @Entity
    void keywordFloat() {
        KeywordType aFloat = new KeywordType("float", new Range.MinMax(1, 2));
        aFloat.setAction((keyword, stepper, scope) -> Float.parseFloat(keyword.getArgument(0, String.class, scope, "Argument must be a string.")));
        context.addKeyword(aFloat);
    }
    
    @Entity
    void keywordLong() {
        KeywordType aLong = new KeywordType("long", new Range.MinMax(1, 2));
        aLong.setAction((keyword, stepper, scope) -> {
            String argument = keyword.getArgument(0, String.class, scope, "Argument must be a string.");
            if (keyword.getArguments().size() == 1) {
                return Long.parseLong(argument);
            }
            else {
                Integer radix = keyword.getArgument(1, Integer.class, scope, "Argument must be an integer.");
                return Long.parseLong(argument, radix);
            }
        });
        context.addKeyword(aLong);
    }
    
    //endregion
    
    @Entity
    void keywordNothing() {
        KeywordType nothing = new KeywordType("nothing", new Range.Any());
        nothing.setAction((keyword, stepper, scope) -> null);
        context.addKeyword(nothing);
    }
    
    @Entity
    void keywordImport() {
        KeywordType anImport = new KeywordType("import", new Range.LowerBound(1));
        anImport.setParseCallback((keyword, context1) -> {
            File source = keyword.getTopContext().getSource();
            if (source == null) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Unable to locate source file of code.");
            }
            List<ShadowSection> arguments = keyword.getArguments();
            for (ShadowSection argument : arguments) {
                if (!(argument instanceof Identifier)) {
                    throw new ShadowExecutionError(keyword.getLine(), argument.getPrimaryToken().getIndex(), "Argument must be an identifier");
                }
            }
            String name = ((Identifier) arguments.get(arguments.size() - 1)).getName();
            context1.findModule(name).ifPresent(c -> {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Module name already exists.");
            });
            String path = arguments.stream().map(section -> ((Identifier) section).getName()).collect(Collectors.joining(File.separator));
            path += ".shd";
            File module = new File(source.getParent(), path);
            if (!module.exists()) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Unable to find specified module.");
            }
            ShadowContext context = ShadowCommons.create(module);
            ShadowParser parser = new ShadowParser(context);
            parser.parse(module);
            if (!context1.addModule(name, context)) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Module name already exists.");
            }
        });
        anImport.setAction((keyword, stepper, scope) -> null);
        context.addKeyword(anImport);
    }
    
    @Entity
    void keywordFrom() {
        KeywordType from = new KeywordType("from", new Range.LowerBound(3));
        from.setParseCallback((keyword, c) -> {
            Identifier sep = keyword.getIdentifier(1);
            if (!sep.getName().equals("do")) {
                throw new ShadowParseError(keyword.getLine(), sep.getPrimaryToken().getIndex(), "Expected \"do\" here.");
            }
        });
        from.setContextTransformer(ContextTransformer.keywordModule(0));
        from.setAction((keyword, stepper, scope) -> keyword.argumentValue(2, scope));
        context.addKeyword(from);
    }
    
    //endregion
    //region Blocks
    
    //region BlockContexts
    
    Stepper findStepper(Stepper stepper, BlockType def, Keyword keyword) {
        Block block = stepper.getBlock();
        while (block.getDefinition() != def) {
            stepper = stepper.getParent();
            if (stepper == null) break;
            block = stepper.getBlock();
        }
        if (stepper == null) {
            throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Could not find block to break from.");
        }
        return stepper;
    }
    
    ShadowContext loopControl(BlockType def) {
        ShadowContext context = new ShadowContext();
        
        KeywordType aBreak = new KeywordType("break", new Range.None());
        aBreak.setAction((keyword, stepper, scope) -> {
            stepper = findStepper(stepper, def, keyword);
            stepper.breakBlock();
            return null;
        });
        context.addKeyword(aBreak);
    
        KeywordType aContinue = new KeywordType("continue", new Range.None());
        aContinue.setAction((keyword, stepper, scope) -> {
            stepper = findStepper(stepper, def, keyword);
            stepper.continueToEnd();
            return null;
        });
        context.addKeyword(aContinue);
    
        return context;
    }
    
    //endregion
    
    @Entity
    void blockMain() {
        BlockType main = new BlockType("main", new Range.None(), new Range.MinMax(0, 1));
        main.setEnterCallback((block, stepper, scope, args) -> {
            List<Identifier> params = block.getParameters();
            if (params.size() == 1) {
                if (args == null) scope.set(params.get(0), new String[0]);
                else scope.set(params.get(0), args.toArray());
            }
        });
        context.addBlock(main);
    }
    
    @Entity
    void blockRepeat() {
        BlockType repeat = new BlockType("repeat", new Range.Single(1), new Range.Single(1));
        repeat.setContextTransformer(ContextTransformer.blockUse(loopControl(repeat)));
        repeat.setPreRunCheck((block, scope, args) -> {
            Integer i = block.getArgument(0, Integer.class, scope, "Modifier should be an integer.");
            if (i < 0) throw ShadowCodeException.exec(block, "Repeat count must be positive.").get();
            if (i == 0) return false;
            scope.setBlockValue(IntStream.range(0, i).iterator());
            return true;
        });
        repeat.setEnterCallback(BlockEnterCallback.iterateParameter(0));
        repeat.setEndCallback(BlockEndCallback.iterateParameter(0));
        context.addBlock(repeat);
    }
    
    @Entity
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
    
    @Entity
    void blockDefine() {
        BlockType define = new BlockType("define", new Range.Single(1), new Range.Any());
        define.setParseCallback((block, c) -> c.addFunction(block));
        setupDefinition(define);
    
        BlockType keyword = new BlockType("keyword", new Range.Single(1), new Range.Any());
        keyword.setParseCallback((b, c) -> {
            Identifier name = (Identifier) b.getArguments().get(0);
            KeywordType keywordType = new KeywordType(name.getName(), new Range.Single(1));
            keywordType.setAction((keyword1, stepper, scope) -> {
                List<Object> params;
                Object o = keyword1.argumentValue(0, scope);
                if (o instanceof Parameters) params = ((Parameters) o).getParams();
                else params = Collections.singletonList(o);
                return b.execute(stepper, new Scope(c, stepper), params);
            });
            if (!c.addKeyword(keywordType)) {
                throw new ShadowExecutionError(b.getLine(), b.argumentIndex(-1), "Keyword already exists.");
            }
        });
        setupDefinition(keyword);
    }
    
    private void setupDefinition(BlockType keyword) {
        keyword.setPreRunCheck((block, scope, args) -> args != null);
        keyword.setEnterCallback(ShadowCommons::argsToParams);
        context.addBlock(keyword);
        
        ShadowContext keywordContext = new ShadowContext();
        keyword.setContextTransformer(ContextTransformer.blockUse(keywordContext));
        KeywordType aReturn1 = new KeywordType("return", new Range.Single(1));
        aReturn1.setAction(returnArgument(keyword));
        keywordContext.addKeyword(aReturn1);
    }
    
    @Entity
    void blockForeach() {
        BlockType foreach = new BlockType("foreach", new Range.Single(1), new Range.Single(1));
        foreach.setContextTransformer(ContextTransformer.blockUse(loopControl(foreach)));
        foreach.setPreRunCheck((block, scope, args) -> {
            Object o = block.argumentValue(0, scope);
            if (o instanceof Iterator) {
                scope.setBlockValue(o);
                return true;
            }
            else if (o instanceof Iterable) {
                scope.setBlockValue(((Iterable) o).iterator());
                return true;
            }
            else if (o.getClass().isArray()) {
                Iterator<?> it = new Iterator<Object>() {
                    private int index;
                    
                    @Override
                    public boolean hasNext() {
                        return index < Array.getLength(o);
                    }
        
                    @Override
                    public Object next() {
                        return Array.get(o, index++);
                    }
                };
                scope.setBlockValue(it);
                return true;
            }
            throw new ShadowExecutionError(block.getLine(), block.argumentIndex(0), "Argument should be an iterator or iterable object.");
        });
        foreach.setEnterCallback(BlockEnterCallback.iterateParameter(0));
        foreach.setEndCallback(BlockEndCallback.iterateParameter(0));
        context.addBlock(foreach);
    }
    
    @Entity
    void blockWhile() {
        BlockType aWhile = new BlockType("while", new Range.Single(1), new Range.None());
        aWhile.setContextTransformer(ContextTransformer.blockUse(loopControl(aWhile)));
        aWhile.setPreRunCheck((block, scope, args) -> {
            return block.getArgument(0, Boolean.class, scope, "Argument must be a boolean.");
        });
        aWhile.setEndCallback((block, stepper, scope) -> {
            Boolean argument = block.getArgument(0, Boolean.class, scope, "Argument must be a boolean.");
            if (argument) stepper.restart();
        });
        context.addBlock(aWhile);
    }
    
    @Entity
    void blockBenchmark() {
        BlockType benchmark = new BlockType("benchmark", new Range.MinMax(0, 1), new Range.None());
        benchmark.setEnterCallback((block, stepper, scope, args) -> {
            scope.setBlockValue(System.nanoTime());
        });
        benchmark.setEndCallback((block, stepper, scope) -> {
            long end = System.nanoTime();
            long start = (long) scope.getBlockValue();
            double time = (double) (end - start) / 1e6;
            if (block.getModifiers().size() == 0) System.out.println(time);
            else scope.set(block.getIdentifier(0), time);
        });
        context.addBlock(benchmark);
    }
    
    @Entity
    void blockUsing() {
        BlockType using = new BlockType("using", new Range.Single(1), new Range.None());
        using.setContextTransformer(ContextTransformer.blockModule(0));
        context.addBlock(using);
    }
    
    //endregion
    
}
