package com.ssplugins.shadow3.commons;

import com.squareup.javapoet.*;
import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.*;
import com.ssplugins.shadow3.def.*;
import com.ssplugins.shadow3.def.custom.NumberCompareOp;
import com.ssplugins.shadow3.def.custom.NumberOperatorType;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.Keyword;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowExecutionError;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.modules.SHDFiles;
import com.ssplugins.shadow3.modules.SHDMath;
import com.ssplugins.shadow3.parsing.ShadowParser;
import com.ssplugins.shadow3.section.*;
import com.ssplugins.shadow3.section.Operator.OpOrder;
import com.ssplugins.shadow3.util.*;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShadowCommons extends ShadowAPI {
    
    public static ShadowContext create(File file) {
        ShadowContext context = new ShadowContext(file);
        new ShadowCommons().loadInto(context);
        context.setName("commons");
        return context;
    }
    
    private ShadowContext context;
    
    private static void argsToParams(Block block, Stepper stepper, Scope scope, List<Object> args) {
        List<Parameter> parameters = block.getParameters();
        if (args.size() != parameters.size()) {
            throw new ShadowExecutionError(block.getLine(), block.argumentIndex(-1), "Number of arguments does not equal number of parameters.");
        }
        for (int i = 0; i < args.size(); ++i) {
            Parameter p = parameters.get(i);
            Object o = args.get(i);
            if (!p.getType().isInstance(o)) {
                throw new ShadowExecutionError(block.getLine(), p.getIdentifier().index(), "Parameter should be " + p.getType().getSimpleName() + ", got " + o.getClass().getSimpleName());
            }
            scope.set(p, o);
        }
    }
    
    private Code generateParameters(GenerateContext c, Keyword keyword, int size, MethodSpec.Builder method, Code base, TypeSpec.Builder type) {
        base.append("(");
        if (keyword.getArguments().size() == size) {
            ShadowSection section = keyword.getArguments().get(size - 1);
            if (section instanceof Compound) {
                base.append(JavaGen.literalParameters((Compound) section, c, type, method));
            }
            else base.append(JavaGen.litArg(c, keyword, size - 1, type, method));
        }
        base.append(")");
        return base;
    }
    
    private Returnable functionReturnable(String name, int args, Function<Keyword, String> target, FunctionSearch search) {
        return (keyword, scope) -> {
            Optional<DefineBlock> define = scope.getContext().findBlock(name).filter(type -> type instanceof DefineBlock).map(type -> (DefineBlock) type);
            if (!define.isPresent()) throw new ShadowException("The function definition blocks have not been added.");
            List<Pair<Class<?>, Integer>> types = null;
            int size = 0;
            if (keyword.getArguments().size() == (args + 1)) {
                ShadowSection section = keyword.getArguments().get(args);
                if (section instanceof Compound) {
                    types = JavaGen.parameterTypeIndex((Compound) section, scope);
                    size = types.size();
                }
                else {
                    types = Collections.singletonList(new Pair<>(section.getReturnType(scope), section.index()));
                    size = 1;
                }
            }
            Block block = search.find(target.apply(keyword), scope, size).orElseThrow(ShadowCodeException.noDef(keyword.getLine(), keyword.argumentIndex(0), "No matching function found."));
            if (size > 0) {
                List<Parameter> parameters = block.getParameters();
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter p = parameters.get(i);
                    if (!p.getType().isAssignableFrom(types.get(i).getLeft())) {
                        throw new ShadowParseError(keyword.getLine(), types.get(i).getRight(), "Incorrect function parameter type. Expecting " + p.getType().getSimpleName());
                    }
                }
            }
            DefineBlock defineBlock = define.get();
            return defineBlock.getReturnType(block, new CompileScope(scope.getContext())).orElse(Returnable.empty());
        };
    }
    
    private List<Code> getGenerations(Class<?> c, GenerateContext context, Keyword keyword, TypeSpec.Builder type, MethodSpec.Builder method) {
        return keyword.getArguments().stream().map(section -> {
            TypeChecker.require(context.getScope(), section, c);
            return section.getGeneration(context, type, method);
        }).collect(Collectors.toList());
    }
    
    private static KeywordAction returnArgument(BlockType def) {
        return (keyword, stepper, scope) -> {
            Scope original = scope;
            while (stepper.getBlock().getDefinition() != def) {
                stepper.breakBlock();
                stepper = stepper.getParent();
                scope = scope.getParent();
            }
            if (keyword.getArguments().size() > 0) {
                scope.setReturnValue(keyword.argumentValue(0, original));
            }
            stepper.breakBlock();
            return null;
        };
    }
    
    private static void pauseConsole() {
        try {
            while (((char) System.in.read()) != '\n') ;
        } catch (IOException e) {
            throw new ShadowException(e);
        }
    }
    
    private static String readConsoleLine() {
        StringBuilder b = new StringBuilder();
        try {
            char c;
            while ((c = (char) System.in.read()) != '\n') {
                if (c == '\r') continue;
                b.append(c);
            }
        } catch (IOException e) {
            throw new ShadowException(e);
        }
        return b.toString();
    }
    
    @Override
    public void loadInto(ShadowContext context) throws ShadowException {
        this.context = context;
        this.callAnnotatedMethods();
        this.context = null;
    }
    
    //region Modules
    
    @Entity
    void moduleMath() {
        context.addLazyModule("math", new SHDMath());
    }
    
    @Entity
    void moduleIO() {
        context.addLazyModule("files", new SHDFiles());
    }
    
    //endregion
    
    //region Types
    
    @Entity
    void commonTypes() {
        context.addType("void", Void.class);
        context.addType("obj", Object.class);
        context.addType("str", String.class);
        context.addType("bool", Boolean.class);
        context.addType("char", Character.class);
        context.addType("int", Integer.class);
        context.addType("long", Long.class);
        context.addType("float", Float.class);
        context.addType("double", Double.class);
    }
    
    //endregion
    
    //region Operators
    
    @Entity
    void operatorComment() {
        context.addOperator(new OperatorType<>("#", OpOrder.ASSIGNMENT, null, null, null, null));
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
    void operatorAssignment() {
        OperatorType<Integer, Object, IndexAssignment> intAssign = new OperatorType<>("=", OpOrder.ASSIGNMENT, Integer.class, Object.class, IndexAssignment.class, IndexAssignment::new);
        context.addOperator(intAssign);
    }
    
    @Entity
    void operatorEquals() {
        OperatorType<Object, Object, Boolean> equals = new OperatorType<>("==", OpOrder.EQUALITY, Object.class, Object.class, boolean.class, Objects::equals);
        equals.setGenerator((leftGen, rightGen, left, right, type, method) -> {
            return Code.format("$T.equals($L, $L)", Objects.class, leftGen, rightGen);
        });
        context.addOperator(equals);
    }
    
    @Entity
    void operatorNotEqual() {
        OperatorType<Object, Object, Boolean> notEqual = new OperatorType<>("!=", OpOrder.EQUALITY, Object.class, Object.class, boolean.class, (o, o2) -> o != o2);
        notEqual.setGenerator(OperatorGen.between("!="));
        context.addOperator(notEqual);
    }
    
    @Entity
    void operatorNot() {
        UnaryOperatorType<Boolean, Boolean> not = new UnaryOperatorType<>("!", boolean.class, boolean.class, b -> !b);
        not.setGenerator(OperatorGen.between("!"));
        context.addOperator(not);
    }
    
    @Entity
    void operatorNegate() {
        UnaryOperatorType<Integer, Integer> nInt = new UnaryOperatorType<>("-", int.class, int.class, i -> -i);
        nInt.setGenerator(OperatorGen.between("-"));
        context.addOperator(nInt);
        UnaryOperatorType<Double, Double> nDouble = new UnaryOperatorType<>("-", double.class, double.class, d -> -d);
        nDouble.setGenerator(OperatorGen.between("-"));
        context.addOperator(nDouble);
        UnaryOperatorType<Long, Long> nLong = new UnaryOperatorType<>("-", long.class, long.class, l -> -l);
        nLong.setGenerator(OperatorGen.between("-"));
        context.addOperator(nLong);
        UnaryOperatorType<Float, Float> nFloat = new UnaryOperatorType<>("-", float.class, float.class, f -> -f);
        nFloat.setGenerator(OperatorGen.between("-"));
        context.addOperator(nFloat);
    }
    
    @Entity
    void operatorAdd() {
        OperatorType<String, Object, String> addString = new OperatorType<>("+", String.class, Object.class, String.class, (a, b) -> a + b.toString());
        addString.setGenerator(OperatorGen.between("+"));
        context.addOperator(addString);
        OperatorType<Object, String, String> addString2 = new OperatorType<>("+", Object.class, String.class, String.class, (a, b) -> a.toString() + b);
        addString2.setMatcher(OperatorType.OperatorMatcher.sameType());
        addString2.setGenerator(OperatorGen.between("+"));
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
        div.setGenerator(OperatorGen.between("/ (double)"));
        context.addOperator(div);
        OperatorType<Number, Number, Integer> intDiv = new OperatorType<>("//", OpOrder.MUL_DIV, Number.class, Number.class, int.class, (a, b) -> a.intValue() / b.intValue());
        intDiv.setGenerator((leftGen, rightGen, left, right, type, method) -> {
            return Code.format("(int) $L / (int) $L", leftGen, rightGen);
        });
        context.addOperator(intDiv);
    }
    
    @Entity
    void operatorExponent() {
        OperatorType<Number, Number, Double> exp = new OperatorType<>("^", Number.class, Number.class, double.class, (a, b) -> Math.pow(a.doubleValue(), b.doubleValue()));
        exp.setLeftToRight(false);
        exp.setGenerator((leftGen, rightGen, left, right, type, method) -> {
            return Code.format("$T.pow($L, $L)", Math.class, leftGen, rightGen);
        });
        context.addOperator(exp);
    }
    
    @Entity
    void operatorModulus() {
        NumberOperatorType modulus = new NumberOperatorType("%", OpOrder.MUL_DIV, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
        modulus.addTo(context);
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
        and.setGenerator(OperatorGen.between("&&"));
        context.addOperator(and);
        OperatorType<Boolean, Boolean, Boolean> or = new OperatorType<>("||", OpOrder.OR, boolean.class, boolean.class, boolean.class, (a, b) -> a || b);
        or.setGenerator(OperatorGen.between("||"));
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
        exit.setGenerator((c, keyword, type, method) -> {
            method.addStatement("$T.exit(0)", System.class);
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
        print.setGenerator((c, keyword, type, method) -> {
            List<Code> arguments = keyword.getArguments()
                                          .stream()
                                          .map(section -> section.getGeneration(c, type, method))
                                          .collect(Collectors.toList());
            if (arguments.size() == 1) {
                JavaGen.printlnValue(method, arguments.get(0));
            }
            else {
                arguments.forEach(code -> {
                    JavaGen.printValue(method, code);
                });
                JavaGen.println(method);
            }
            return null;
        });
        context.addKeyword(print);
    }
    
    @Entity
    void keywordSet() {
        KeywordType set = new KeywordType("set", new Range.Of(2, 4));
        set.setAction((keyword, stepper, scope) -> {
            Identifier name = keyword.getIdentifier(0);
            if (keyword.getArguments().size() == 2) {
                Object o = keyword.argumentValue(1, scope);
                scope.set(name, o);
                return o;
            }
            Operator.requireComma(keyword, 1);
            Identifier type = keyword.getIdentifier(2);
            Class<?> c = scope.getContext().getType(keyword, type.getName());
            Object o = keyword.argumentValue(3, scope);
            if (o != null && !NumberType.isAssignableFrom(c, o.getClass())) {
                throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(3), "Argument type doesn't match declared type.");
            }
            scope.set(name, o);
            return o;
        });
        set.setReturnable((keyword, scope) -> {
            Identifier name = keyword.getIdentifier(0);
            Class<?> expected;
            if (keyword.getArguments().size() == 2) {
                expected = keyword.getArguments().get(1).getReturnType(scope);
            }
            else {
                expected = scope.getContext().findType(keyword.getIdentifier(2).getName()).orElseThrow(() -> new ShadowParseError(keyword.getLine(), keyword.argumentIndex(2), "Type not defined."));
            }
            scope.checkMark(name.getValidName(), keyword);
            Pair<Boolean, Class<?>> result = scope.addCheck(name.getName(), expected);
            if (!result.getLeft()) {
                throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(1), "Invalid assignment. Expected type: " + result.getRight().getSimpleName());
            }
            return result.getRight();
        });
        set.setGenerator((c, keyword, type, method) -> {
            CompileScope scope = c.getScope();
            String name = keyword.getIdentifier(0).getName();
            Code value;
            Code cast = null;
            Class<?> returnType = keyword.getReturnType();
            if (keyword.getArguments().size() == 2) {
                value = JavaGen.litArg(c, keyword, 1, type, method);
            }
            else {
                value = JavaGen.litArg(c, keyword, 3, type, method);
                cast = Code.format(" ($T)", returnType);
            }
            if (name.equals(value.toString())) return value;
            Code code = Code.empty();
            if (!scope.isMarked(name)) code.append("$T ", returnType);
            code.append("$L =", name);
            if (cast != null) code.append(cast);
            code.append(" $L", value);
            scope.addCheck(name, returnType);
            code.addTo(method);
            return Code.plain(name);
        });
        context.addKeyword(set);
    }
    
    @Entity
    void keywordType() {
        KeywordType type = new KeywordType("type", new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Object o = keyword.argumentValue(0, scope);
            if (o == null) return "null";
            return o.getClass().getSimpleName();
        });
        type.setReturnable(Returnable.of(String.class));
        type.setGenerator((c, keyword, type1, method) -> {
            String name = c.getComponentName("type");
            c.checkName(name, s -> {
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(String.class)
                                            .addParameter(Object.class, "o")
                                            .addStatement("if (o == null) return \"null\"")
                                            .addStatement("return o.getClass().getSimpleName()")
                                            .build();
                type1.addMethod(spec);
            });
            Code value = keyword.getArguments().get(0).getGeneration(c, type1, method);
            return Code.format("$L($L)", name, value);
        });
        context.addKeyword(type);
    }
    
    @Entity
    void keywordPause() {
        KeywordType pause = new KeywordType("pause", new Range.None());
        pause.setAction((keyword, stepper, scope) -> {
            pauseConsole();
            return null;
        });
        pause.setGenerator((c, keyword, type, method) -> {
            String name = c.getComponentName("pause");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(void.class)
                                            .beginControlFlow("try")
                                            .addStatement("while (((char) $T.in.read()) != '\\n')", System.class)
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"IO error while console was paused.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L()", name);
        });
        pause.setStatementMode(true);
        context.addKeyword(pause);
    }
    
    @Entity
    void keywordExec() {
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
            Scope bs = new Scope(scope.getContext(), stepper);
            Object value = block.execute(stepper, bs, params);
            scope.getCallbacks().addAll(bs.getCallbacks());
            return value;
        });
        exec.setReturnable(functionReturnable("define", 1, keyword -> keyword.getIdentifier(0).getName(), (name, scope, size) -> scope.getContext().findFunction(name, size)));
        exec.setGenerator((c, keyword, type, method) -> {
            Code code = Code.empty();
            code.append(c.getComponentName("def_" + keyword.getIdentifier(0).getName()));
            return generateParameters(c, keyword, 2, method, code, type);
        });
        exec.setStatementMode(true);
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
            return ((String) o).chars().mapToObj(i -> (char) i).toArray(Character[]::new);
        });
        chars.setReturnable(Returnable.of(Character[].class));
        chars.setGenerator((c, keyword, type, method) -> {
            TypeChecker.require(c.getScope(), keyword.getArguments().get(0), String.class);
            String name = c.getComponentName("chars");
            c.checkName(name, s -> {
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(Character[].class)
                                            .addParameter(String.class, "s")
                                            .addStatement(CodeBlock.of("return s.chars().mapToObj(i -> (char) i).toArray($T[]::new)", Character.class))
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, JavaGen.litArg(c, keyword, 0, type, method));
        });
        context.addKeyword(chars);
    }
    
    @Entity
    void keywordCount() {
        KeywordType count = new KeywordType("count", new Range.MinMax(1, 3));
        count.setAction((keyword, stepper, scope) -> {
            int args = keyword.getArguments().size();
            int start = keyword.getInt(0, scope);
            if (args == 1) return IntStream.range(0, start).iterator();
            int stop = keyword.getInt(1, scope);
            if (args == 2) return IntStream.range(start, stop).iterator();
            int step = keyword.getInt(2, scope);
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
        count.setReturnable(Returnable.of(Iterator.class));
        count.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), Integer.class);
            Code start = args.get(0).getGeneration(c, type, method);
            if (args.size() == 1) {
                return Code.format("$T.range(0, $L).iterator()", IntStream.class, start);
            }
            TypeChecker.require(c.getScope(), args.get(1), Integer.class);
            Code stop = args.get(1).getGeneration(c, type, method);
            if (args.size() == 2) {
                return Code.format("$T.range($L, $L).iterator()", IntStream.class, start, stop);
            }
            TypeChecker.require(c.getScope(), args.get(2), Integer.class);
            Code step = args.get(2).getGeneration(c, type, method);
            String name = c.getComponentName("count");
            c.checkName(name, s -> {
                TypeSpec typeSpec = TypeSpec.anonymousClassBuilder("")
                                            .addSuperinterface(ParameterizedTypeName.get(Iterator.class, Integer.class))
                                            .addField(FieldSpec.builder(int.class, "value", Modifier.PRIVATE).initializer("start").build())
                                            .addMethod(MethodSpec.methodBuilder("hasNext")
                                                                 .addAnnotation(Override.class)
                                                                 .addModifiers(Modifier.PUBLIC)
                                                                 .returns(boolean.class)
                                                                 .addStatement("return value < stop")
                                                                 .build())
                                            .addMethod(MethodSpec.methodBuilder("next")
                                                                 .addAnnotation(Override.class)
                                                                 .addModifiers(Modifier.PUBLIC)
                                                                 .returns(Integer.class)
                                                                 .addStatement("int r = value")
                                                                 .addStatement("value += step")
                                                                 .addStatement("return r")
                                                                 .build())
                                            .build();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(Iterator.class)
                                            .addParameter(int.class, "start")
                                            .addParameter(int.class, "stop")
                                            .addParameter(int.class, "step")
                                            .addStatement("return $L", typeSpec)
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L, $L, $L)", name, start, stop, step);
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
    void keywordHSet() {
        HSetKeyword hSet = new HSetKeyword();
        context.addKeyword(hSet);
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
        len.setReturnable(Returnable.of(Integer.class));
        len.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            Code gen = section.getGeneration(c, type, method);
            Class<?> returnType = section.getReturnType(c.getScope());
            if (String.class.isAssignableFrom(returnType)) {
                return Code.format("$L.length()", gen);
            }
            else if (returnType.isArray()) {
                return Code.format("$L.length", gen);
            }
            else if (Collection.class.isAssignableFrom(returnType)) {
                return Code.format("$L.size()", gen);
            }
            else if (Map.class.isAssignableFrom(returnType)) {
                return Code.format("$L.size()", gen);
            }
            throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(0), "Argument must be a string, array, or collection.");
        });
        context.addKeyword(len);
    }
    
    @Entity
    void keywordSleep() {
        KeywordType sleep = new KeywordType("sleep", new Range.Single(1));
        sleep.setAction((keyword, stepper, scope) -> {
            Object o = keyword.argumentValue(0, scope);
            TypeChecker.require(keyword.getArguments().get(0), NumberType.isIntegerType(o.getClass()), "Argument must be an integer type.");
            long time = ((Number) o).longValue();
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new ShadowExecutionError(keyword.getLine(), keyword.argumentIndex(-1), "Sleep interrupted.");
            }
            return null;
        });
        sleep.setGenerator((c, keyword, type, method) -> {
            String name = c.getComponentName("sleep");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(void.class)
                                            .addParameter(Long.class, "time")
                                            .beginControlFlow("try")
                                            .addStatement("$T.sleep(time)", Thread.class)
                                            .nextControlFlow("catch ($T $L)", InterruptedException.class, tmp)
                                            .addStatement("throw new $T(\"Sleep interrupted.\", $L)", tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(section, NumberType.isIntegerType(section.getReturnType(c.getScope())), "Argument must be an integer type.");
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        sleep.setStatementMode(true);
        context.addKeyword(sleep);
    }
    
    @Entity
    void keywordInput() {
        KeywordType input = new KeywordType("input", new Range.MinMax(0, 1));
        input.setAction((keyword, stepper, scope) -> {
            if (keyword.getArguments().size() == 1) {
                System.out.print(keyword.argumentValue(0, scope));
            }
            return readConsoleLine();
        });
        input.setReturnable(Returnable.of(String.class));
        input.setGenerator((c, keyword, type, method) -> {
            String name = c.getComponentName("input");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(String.class)
                                            .addStatement("$1T b = new $1T()", StringBuilder.class)
                                            .beginControlFlow("try")
                                            .addStatement("char c")
                                            .beginControlFlow("while ((c = (char) System.in.read()) != '\\n')")
                                            .addStatement("if (c == '\\r') continue")
                                            .addStatement("b.append(c)")
                                            .endControlFlow()
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Unable to read from console.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .addStatement("return b.toString()")
                                            .build();
                type.addMethod(spec);
                MethodSpec spec2 = MethodSpec.methodBuilder(s)
                                             .returns(String.class)
                                             .addParameter(Object.class, "prompt")
                                             .addStatement("$T.out.print(prompt)", System.class)
                                             .addStatement("return $L()", s)
                                             .build();
                type.addMethod(spec2);
            });
            String tmp = c.getScope().nextTemp();
            Code code = Code.empty();
            code.append("$T $L = ", String.class, tmp);
            code.append(name).append("(");
            if (keyword.getArguments().size() == 1) {
               code.append(JavaGen.litArg(c, keyword, 0, type, method));
            }
            code.append(")");
            code.addTo(method);
            return Code.plain(tmp);
        });
        input.setStatementMode(true);
        context.addKeyword(input);
    }
    
    @Entity
    void keywordRandom() {
        KeywordType random = new KeywordType("random", new Range.MinMax(0, 2));
        random.setAction((keyword, stepper, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 0) return ThreadLocalRandom.current().nextDouble();
            if (args.size() == 1) {
                int bound = keyword.getInt(0, scope);
                return ThreadLocalRandom.current().nextInt(bound);
            }
            int lower = keyword.getInt(0, scope);
            int upper = keyword.getInt(1, scope);
            return ThreadLocalRandom.current().nextInt(lower, upper);
        });
        random.setReturnable((keyword, scope) -> {
            if (keyword.getArguments().size() == 0) return Double.class;
            return Integer.class;
        });
        random.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 0) {
                return Code.format("$T.current().nextDouble()", ThreadLocalRandom.class);
            }
            else if (args.size() == 1) {
                TypeChecker.require(c.getScope(), args.get(0), Integer.class);
                Code lower = args.get(0).getGeneration(c, type, method);
                return Code.format("$T.current().nextInt($L)", ThreadLocalRandom.class, lower);
            }
            TypeChecker.require(c.getScope(), args.get(0), Integer.class);
            TypeChecker.require(c.getScope(), args.get(1), Integer.class);
            Code lower = args.get(0).getGeneration(c, type, method);
            Code upper = args.get(1).getGeneration(c, type, method);
            return Code.format("$T.current().nextInt($L, $L)", ThreadLocalRandom.class, lower, upper);
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
        aTry.setReturnable((keyword, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            Class<?> a = args.get(0).getReturnType(scope);
            Class<?> b = args.get(2).getReturnType(scope);
            if (a.isAssignableFrom(b) || b == Void.class) return a;
            return Object.class;
        });
        aTry.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            Class<?> returnType = keyword.getReturnType();
            String tmp = c.getScope().nextTemp();
            method.addStatement("$T $L", returnType, tmp)
                  .beginControlFlow("try");
            Code value = args.get(0).getGeneration(c, type, method);
            method.addStatement("$L = $L", tmp, value)
                  .nextControlFlow("catch ($T $L)", Exception.class, c.getScope().nextTemp());
            value = args.get(2).getGeneration(c, type, method);
            Class<?> errType = args.get(2).getReturnType(c.getScope());
            if (errType != Void.class) {
                method.addStatement("$L = $L", tmp, value);
            }
            else if (args.get(2) instanceof InlineKeyword) {
                if (((InlineKeyword) args.get(2)).getKeyword().getDefinition().isStatementMode()) {
                    value.addTo(method);
                }
                method.addStatement("$L = null", tmp);
            }
            method.endControlFlow();
            return Code.plain(tmp);
        });
        context.addKeyword(aTry);
    }
    
    @Entity
    void keywordMulti() {
        KeywordType multi = new KeywordType("multi", new Range.LowerBound(1));
        multi.setAction((keyword, stepper, scope) -> {
            List<Object> objects = keyword.argumentValues(scope);
            return objects.get(objects.size() - 1);
        });
        multi.setReturnable((keyword, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            return args.get(args.size() - 1).getReturnType(scope);
        });
        multi.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            for (int i = 0; i < args.size(); i++) {
                if (i == args.size() - 1) {
                    return args.get(i).getGeneration(c, type, method);
                }
                args.get(i).getGeneration(c, type, method);
            }
            // unreachable
            return null;
        });
        context.addKeyword(multi);
    }
    
    @Entity
    void keywordJava() {
        KeywordType java = new KeywordType("java", new Range.LowerBound(2));
        java.setParseCallback((keyword, context1) -> {
            Identifier name = keyword.getIdentifier(0);
            List<ShadowSection> args = keyword.getArguments();
            StringBuilder identifier = new StringBuilder(args.stream().skip(1).map(section -> section.getPrimaryToken().getRaw()).collect(Collectors.joining()));
            int dim = 0;
            while (identifier.substring(identifier.length() - 2).equals("[]")) {
                dim++;
                identifier.setLength(identifier.length() - 2);
            }
            if (dim > 0) {
                identifier.insert(0, "L");
                while (dim-- > 0) {
                    identifier.insert(0, "[");
                }
                identifier.append(";");
            }
            try {
                Class<?> type = Class.forName(identifier.toString());
                context1.addType(name.getName(), type);
            } catch (ClassNotFoundException e) {
                throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(1), "Class not found.");
            }
        });
        java.setAction((keyword, stepper, scope) -> null);
        java.setGenerator(KeywordGen.none());
        context.addKeyword(java);
    }
    
    @Entity
    void keywordCast() {
        KeywordType cast = new KeywordType("cast", new Range.Single(3));
        cast.setAction((keyword, stepper, scope) -> {
            Operator.requireComma(keyword, 1);
            return keyword.argumentValue(2, scope);
        });
        cast.setReturnable((keyword, scope) -> {
            return scope.getContext().getType(keyword, keyword.getIdentifier(0).getName());
        });
        cast.setGenerator((c, keyword, type, method) -> {
            Class<?> t = c.getFullContext().getType(keyword, keyword.getIdentifier(0).getName());
            ShadowSection section = keyword.getArguments().get(2);
            return Code.format("(($T) $L)", t, section.getGeneration(c, type, method));
        });
        context.addKeyword(cast);
    }
    
    //region String
    
    @Entity
    void keywordStartsWith() {
        KeywordType startsWith = new KeywordType("starts_with", new Range.Single(2));
        startsWith.setAction((keyword, stepper, scope) -> {
            return keyword.getString(0, scope).startsWith(keyword.getString(1, scope));
        });
        startsWith.setReturnable(Returnable.of(Boolean.class));
        startsWith.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            TypeChecker.require(c.getScope(), args.get(1), String.class);
            return Code.format("$L.startsWith($L)", args.get(0).getGeneration(c, type, method), args.get(1).getGeneration(c, type, method));
        });
        context.addKeyword(startsWith);
    }
    
    @Entity
    void keywordEndsWith() {
        KeywordType endsWith = new KeywordType("ends_with", new Range.Single(2));
        endsWith.setAction((keyword, stepper, scope) -> {
            return keyword.getString(0, scope).endsWith(keyword.getString(1, scope));
        });
        endsWith.setReturnable(Returnable.of(Boolean.class));
        endsWith.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            TypeChecker.require(c.getScope(), args.get(1), String.class);
            return Code.format("$L.endsWith($L)", args.get(0).getGeneration(c, type, method), args.get(1).getGeneration(c, type, method));
        });
        context.addKeyword(endsWith);
    }
    
    @Entity
    void keywordContains() {
        KeywordType contains = new KeywordType("contains", new Range.Single(2));
        contains.setAction((keyword, stepper, scope) -> {
            return keyword.getString(0, scope).contains(keyword.getString(1, scope));
        });
        contains.setReturnable(Returnable.of(Boolean.class));
        contains.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            TypeChecker.require(c.getScope(), args.get(1), String.class);
            return Code.format("$L.contains($L)", args.get(0).getGeneration(c, type, method), args.get(1).getGeneration(c, type, method));
        });
        context.addKeyword(contains);
    }
    
    @Entity
    void keywordSplit() {
        KeywordType split = new KeywordType("split", new Range.MinMax(2, 3));
        split.setAction((keyword, stepper, scope) -> {
            String string = keyword.getString(0, scope);
            String s = keyword.getString(1, scope);
            if (keyword.getArguments().size() > 2) {
                int limit = keyword.getInt(2, scope);
                return string.split(s, limit);
            }
            else return string.split(s);
        });
        split.setReturnable(Returnable.of(String[].class));
        split.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            TypeChecker.require(c.getScope(), args.get(1), String.class);
            Code s = args.get(0).getGeneration(c, type, method);
            Code regex = args.get(1).getGeneration(c, type, method);
            if (args.size() > 2) {
                TypeChecker.require(c.getScope(), args.get(2), Integer.class);
                Code limit = args.get(2).getGeneration(c, type, method);
                return Code.format("$L.split($L, $L)", s, regex, limit);
            }
            return Code.format("$L.split($L)", s, regex);
        });
        context.addKeyword(split);
    }
    
    @Entity
    void keywordReplace() {
        KeywordType replace = new KeywordType("replace", new Range.Single(3));
        replace.setAction((keyword, stepper, scope) -> {
            String string = keyword.getString(0, scope);
            String find = keyword.getString(1, scope);
            String rep = keyword.getString(2, scope);
            return string.replace(find, rep);
        });
        replace.setReturnable(Returnable.of(String.class));
        replace.setGenerator((c, keyword, type, method) -> {
            List<Code> gen = getGenerations(String.class, c, keyword, type, method);
            return Code.format("$L.replace($L, $L)", gen.get(0), gen.get(1), gen.get(2));
        });
        context.addKeyword(replace);
    }
    
    @Entity
    void keywordReplaceAll() {
        KeywordType replaceAll = new KeywordType("replace_all", new Range.Single(3));
        replaceAll.setAction((keyword, stepper, scope) -> {
            String string = keyword.getString(0, scope);
            String find = keyword.getString(1, scope);
            String rep = keyword.getString(2, scope);
            return string.replaceAll(find, rep);
        });
        replaceAll.setReturnable(Returnable.of(String.class));
        replaceAll.setGenerator((c, keyword, type, method) -> {
            List<Code> gen = getGenerations(String.class, c, keyword, type, method);
            return Code.format("$L.replaceAll($L, $L)", gen.get(0), gen.get(1), gen.get(2));
        });
        context.addKeyword(replaceAll);
    }
    
    @Entity
    void keywordEmpty() {
        KeywordType empty = new KeywordType("empty", new Range.Single(1));
        empty.setAction((keyword, stepper, scope) -> {
            String argument = keyword.getString(0, scope);
            return argument.isEmpty();
        });
        empty.setReturnable(Returnable.of(Boolean.class));
        empty.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            return Code.format("$L.isEmpty()", args.get(0).getGeneration(c, type, method));
        });
        context.addKeyword(empty);
    }
    
    @Entity
    void keywordSubstr() {
        KeywordType substr = new KeywordType("substr", new Range.MinMax(2, 3));
        substr.setAction((keyword, stepper, scope) -> {
            String s = keyword.getString(0, scope);
            int start = keyword.getInt(1, scope);
            if (keyword.getArguments().size() == 3) {
                int end = keyword.getInt(2, scope);
                return s.substring(start, end);
            }
            return s.substring(start);
        });
        substr.setReturnable(Returnable.of(String.class));
        substr.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            Code string = args.get(0).getGeneration(c, type, method);
            Code start = args.get(1).getGeneration(c, type, method);
            if (args.size() == 3) {
                Code end = args.get(2).getGeneration(c, type, method);
                return Code.format("$L.substring($L, $L)", string, start, end);
            }
            return Code.format("$L.substring($L)", string, start);
        });
        context.addKeyword(substr);
    }
    
    //endregion
    
    //region Conversions
    
    @Entity
    void keywordStr() {
        KeywordType str = new KeywordType("str", new Range.Single(1));
        str.setAction((keyword, stepper, scope) -> keyword.argumentValue(0, scope).toString());
        str.setReturnable(Returnable.of(String.class));
        str.setGenerator((c, keyword, type, method) -> {
            return Code.format("$T.toString($L)", Objects.class, keyword.getArguments().get(0).getGeneration(c, type, method));
        });
        context.addKeyword(str);
    }
    
    @Entity
    void keywordInt() {
        KeywordType anInt = new KeywordType("int", new Range.MinMax(1, 2));
        anInt.setAction((keyword, stepper, scope) -> {
            String argument = keyword.getString(0, scope);
            if (keyword.getArguments().size() == 1) {
                return Integer.parseInt(argument);
            }
            else {
                int radix = keyword.getInt(1, scope);
                return Integer.parseInt(argument, radix);
            }
        });
        anInt.setReturnable(Returnable.of(Integer.class));
        anInt.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            Code value = args.get(0).getGeneration(c, type, method);
            if (args.size() == 1) {
                return Code.format("$T.parseInt($L)", Integer.class, value);
            }
            TypeChecker.require(c.getScope(), args.get(1), Integer.class);
            Code radix = args.get(1).getGeneration(c, type, method);
            return Code.format("$T.parseInt($L, $L)", Integer.class, value, radix);
        });
        context.addKeyword(anInt);
    }
    
    @Entity
    void keywordDouble() {
        KeywordType aDouble = new KeywordType("double", new Range.Single(1));
        aDouble.setAction((keyword, stepper, scope) -> Double.parseDouble(keyword.getString(0, scope)));
        aDouble.setReturnable(Returnable.of(Double.class));
        aDouble.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, String.class);
            return Code.format("$T.parseDouble($L)", Double.class, section.getGeneration(c, type, method));
        });
        context.addKeyword(aDouble);
    }
    
    @Entity
    void keywordFloat() {
        KeywordType aFloat = new KeywordType("float", new Range.MinMax(1, 2));
        aFloat.setAction((keyword, stepper, scope) -> Float.parseFloat(keyword.getString(0, scope)));
        aFloat.setReturnable(Returnable.of(Float.class));
        aFloat.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, String.class);
            return Code.format("$T.parseFloat($L)", Float.class, section.getGeneration(c, type, method));
        });
        context.addKeyword(aFloat);
    }
    
    @Entity
    void keywordLong() {
        KeywordType aLong = new KeywordType("long", new Range.MinMax(1, 2));
        aLong.setAction((keyword, stepper, scope) -> {
            String argument = keyword.getString(0, scope);
            if (keyword.getArguments().size() == 1) {
                return Long.parseLong(argument);
            }
            else {
                int radix = keyword.getInt(1, scope);
                return Long.parseLong(argument, radix);
            }
        });
        aLong.setReturnable(Returnable.of(Long.class));
        aLong.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), String.class);
            Code value = args.get(0).getGeneration(c, type, method);
            if (args.size() == 1) {
                return Code.format("$T.parseLong($L)", Long.class, value);
            }
            TypeChecker.require(c.getScope(), args.get(1), Integer.class);
            Code radix = args.get(1).getGeneration(c, type, method);
            return Code.format("$T.parseLong($L, $L)", Long.class, value, radix);
        });
        context.addKeyword(aLong);
    }
    
    //endregion
    
    @Entity
    void keywordNothing() {
        KeywordType nothing = new KeywordType("nothing", new Range.MinMax(0, 1));
        nothing.setAction((keyword, stepper, scope) -> {
            if (keyword.getArguments().size() == 0) return null;
            return keyword.argumentValue(0, scope) == null;
        });
        nothing.setReturnable((keyword, scope) -> {
            if (keyword.getArguments().size() == 0) return Returnable.empty();
            return Boolean.class;
        });
        nothing.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 0) return Code.plain("null");
            return Code.format("($L == null)", args.get(0).getGeneration(c, type, method));
        });
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
            
            String jarPath = path + ".jar";
            File jarModule = new File(source.getParent(), jarPath);
            if (jarModule.exists()) {
                try {
                    ServiceLoader<ShadowImport> loader = ServiceLoader.load(ShadowImport.class, new URLClassLoader(new URL[] {jarModule.toURI().toURL()}, this.getClass().getClassLoader()));
                    context1.addModule(name, loader.iterator().next().getModuleContext());
                } catch (MalformedURLException e) {
                    throw new ShadowException(e);
                }
                return;
            }
            
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
        anImport.setGenerator(KeywordGen.none());
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
            Identifier name = keyword.getIdentifier(0);
            c.pokeModule(name.getName());
        });
        from.setContextTransformer(ContextTransformer.keywordModule(0));
        from.setAction((keyword, stepper, scope) -> keyword.argumentValue(2, scope));
        from.setReturnable((keyword, scope) -> {
            return keyword.getArguments().get(2).getReturnType(scope);
        });
        from.setGenerator((c, keyword, type, method) -> {
            return keyword.getArguments().get(2).getGeneration(c, type, method);
        });
        context.addKeyword(from);
    }
    
    //endregion
    //region Blocks
    
    //region BlockContexts
    
    public static Stepper findStepper(Stepper stepper, BlockType def, Keyword keyword, Consumer<Stepper> steps) {
        Block block = stepper.getBlock();
        while (block.getDefinition() != def) {
            if (steps != null) steps.accept(stepper);
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
        context.setName("loopControl");
        
        KeywordType aBreak = new KeywordType("break", new Range.None());
        aBreak.setAction((keyword, stepper, scope) -> {
            stepper = findStepper(stepper, def, keyword, Stepper::breakBlock);
            stepper.breakBlock();
            return null;
        });
        aBreak.setReturnable(Returnable.none());
        aBreak.setGenerator((c, keyword, type, method) -> Code.plain("break"));
        aBreak.setStatementMode(true);
        context.addKeyword(aBreak);
        
        KeywordType aContinue = new KeywordType("continue", new Range.None());
        aContinue.setAction((keyword, stepper, scope) -> {
            stepper = findStepper(stepper, def, keyword, Stepper::breakBlock);
            stepper.continueToEnd();
            return null;
        });
        aContinue.setReturnable(Returnable.none());
        aContinue.setGenerator((c, keyword, type, method) -> Code.plain("continue"));
        aContinue.setStatementMode(true);
        context.addKeyword(aContinue);
        
        return context;
    }
    
    //endregion
    
    @Entity
    void blockMain() {
        BlockType main = new BlockType("main", new Range.None(), new Range.MinMax(0, 1));
        main.setEnterCallback((block, stepper, scope, args) -> {
            List<Parameter> params = block.getParameters();
            if (params.size() == 1) {
                if (args == null) scope.set(params.get(0), new String[0]);
                else scope.set(params.get(0), args.toArray());
            }
        });
        main.setParamLookup(ParamLookup.constant(String[].class));
        main.setGenerator((c, block, type, method) -> {
            MethodSpec entry = MethodSpec.methodBuilder("main")
                                         .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                         .returns(void.class)
                                         .addParameter(String[].class, "args")
                                         .addStatement("new $L()._main(args)", block.getTopContext().getFileName())
                                         .build();
            type.addMethod(entry);
            
            List<Parameter> params = block.getParameters();
            MethodSpec.Builder start = MethodSpec.methodBuilder("_main")
                                                 .returns(void.class)
                                                 .addParameter(String[].class, (params.size() == 1 ? params.get(0).getName() : "$args"));
            block.addBody(c, type, start);
            type.addMethod(start.build());
        });
        context.addBlock(main);
    }
    
    @Entity
    void blockRepeat() {
        BlockType repeat = new BlockType("repeat", new Range.Single(1), new Range.Single(1));
        repeat.setContextTransformer(ContextTransformer.blockUse(loopControl(repeat)));
        repeat.setPreRunCheck((block, scope, args) -> {
            int i = block.getInt(0, scope);
            if (i < 0) throw ShadowCodeException.exec(block, "Repeat count must be positive.").get();
            if (i == 0) return false;
            scope.setBlockValue(IntStream.range(0, i).iterator());
            return true;
        });
        repeat.setEnterCallback(BlockEnterCallback.iterateParameter(0));
        repeat.setEndCallback(BlockEndCallback.iterateParameter(0));
        repeat.setParamLookup(ParamLookup.constant(Integer.class));
        repeat.setGenerator((c, block, type, method) -> {
            String v = block.getParameters().get(0).getIdentifier().getName();
            Code value = block.getArguments().get(0).getGeneration(c, type, method);
            method.beginControlFlow("for (int $L = 0; $L < $L; $L++)", v, v, value, v);
            block.addBody(c, type, method);
            method.endControlFlow();
        });
        context.addBlock(repeat);
    }
    
    @Entity
    @SuppressWarnings("Duplicates")
    void blockConditionals() {
        BlockType typeIf = new BlockType("if", new Range.Single(1), new Range.None());
        typeIf.setPreRunCheck((block, scope, args) -> {
            return block.getBoolean(0, scope);
        });
        Schema<ShadowEntity> skipElse = new Schema<>(e -> e.flow().isBlock("elseif", "else"));
        typeIf.setEnterCallback((block, stepper, scope, args) -> {
            stepper.setSkipSchema(skipElse);
        });
        typeIf.setGenerator((c, block, type, method) -> {
            Code code = block.getArguments().get(0).getGeneration(c, type, method);
            method.beginControlFlow("if ($L)", code.toCodeBlock());
            block.addBody(c, type, method);
            method.endControlFlow();
        });
        
        Schema<Block> condSchema = new Schema<>(block -> block.flow().prevIsBlock("if", "elseif"));
        condSchema.setSituation("Block must follow an if or elseif block.");
        
        BlockType elseif = new BlockType("elseif", new Range.Single(1), new Range.None());
        elseif.setSchema(condSchema);
        elseif.setPreRunCheck((block, scope, args) -> {
            return block.getBoolean(0, scope);
        });
        elseif.setEnterCallback((block, stepper, scope, args) -> {
            stepper.setSkipSchema(skipElse);
        });
        elseif.setGenerator((c, block, type, method) -> {
            method.beginControlFlow("elseif ($L)", block.getArguments().get(0).getGeneration(c, type, method));
            block.addBody(c, type, method);
            method.endControlFlow();
        });
        
        BlockType typeElse = new BlockType("else", new Range.None(), new Range.None());
        typeElse.setSchema(condSchema);
        typeElse.setGenerator((c, block, type, method) -> {
            method.beginControlFlow("else");
            block.addBody(c, type, method);
            method.endControlFlow();
        });
        
        context.addBlock(typeIf);
        context.addBlock(elseif);
        context.addBlock(typeElse);
    }
    
    @Entity
    void blockDefine() {
        BlockType define = new DefineBlock("define");
        define.setParseCallback((block, c) -> {
            DefineBlock.getDeclaredType(block, c);
            c.addFunction(block);
        });
        setupDefinition(define, false);
        
        BlockType keyword = new DefineBlock("keyword");
        keyword.setParseCallback((b, c) -> {
            DefineBlock.getDeclaredType(b, c);
            Identifier name = (Identifier) b.getArguments().get(0);
            KeywordType keywordType = new KeywordType(name.getName(), new Range.MinMax(0, 1));
            keywordType.setAction((keyword1, stepper, scope) -> {
                List<Object> params;
                if (keyword1.getArguments().size() == 0) params = Collections.emptyList();
                else {
                    Object o = keyword1.argumentValue(0, scope);
                    if (o instanceof Parameters) params = ((Parameters) o).getParams();
                    else params = Collections.singletonList(o);
                }
                Scope bs = new Scope(c, stepper);
                Object value = b.execute(stepper, bs, params);
                scope.getCallbacks().addAll(bs.getCallbacks());
                return value;
            });
            keywordType.setReturnable(functionReturnable("keyword", 0, Keyword::getName, (n, scope, size) -> Optional.of(b)));
            keywordType.setGenerator((c1, keyword1, type, method) -> {
                Code code = Code.empty();
                code.append(c1.getComponentName("kdef_" + keyword1.getName()));
                return generateParameters(c1, keyword1, 1, method, code, type);
            });
            keywordType.setStatementMode(true);
            if (!c.addKeyword(keywordType)) {
                throw new ShadowExecutionError(b.getLine(), b.argumentIndex(-1), "Keyword already exists.");
            }
        });
        setupDefinition(keyword, true);
    }
    
    private void setupDefinition(BlockType keyword, boolean direct) {
        keyword.setPreRunCheck((block, scope, args) -> args != null);
        keyword.setEnterCallback(ShadowCommons::argsToParams);
        keyword.setGenerator((c, block, type, method) -> {
            String name = c.getComponentName((direct ? "k" : "") + "def_" + block.getIdentifier(0).getName());
            if (c.nameExists(name)) {
                throw new ShadowParseError(block.getLine(), block.argumentIndex(0), "Duplicate function definition.");
            }
            c.addName(name);
            Optional<Class<?>> returnType = ((DefineBlock) block.getDefinition()).getReturnType(block, new CompileScope(c.getFullContext()));
            MethodSpec.Builder spec = MethodSpec.methodBuilder(name)
                                                .returns(returnType.filter(rt -> rt != Void.class).orElse(void.class));
            block.getParameters().forEach(param -> spec.addParameter(param.getType(), param.getName()));
            block.addBody(c, type, spec);
            type.addMethod(spec.build());
        });
        context.addBlock(keyword);
        
        ShadowContext keywordContext = new ShadowContext();
        keywordContext.setName("defContext");
        keyword.setContextTransformer(ContextTransformer.blockUse(keywordContext));
        KeywordType aReturn1 = new KeywordType("return", new Range.MinMax(0, 1));
        aReturn1.setReturnable((keyword1, scope) -> {
            List<ShadowSection> args = keyword1.getArguments();
            if (args.size() == 0) return Returnable.empty();
            return args.get(0).getReturnType(scope);
        });
        aReturn1.setGenerator((c, keyword1, type, method) -> {
            if (keyword1.getArguments().size() == 0) method.addStatement("return");
            else method.addStatement("return $L", JavaGen.litArg(c, keyword1, 0, type, method));
            return null;
        });
        aReturn1.setAction(returnArgument(keyword));
        keywordContext.addKeyword(aReturn1);
    }
    
    @Entity
    void blockForeach() {
        BlockType foreach = new BlockType("foreach", new Range.Single(1), new Range.Single(1));
        foreach.setContextTransformer(ContextTransformer.blockUse(loopControl(foreach)));
        foreach.setPreRunCheck((block, scope, args) -> {
            Object o = block.argumentValue(0, scope);
            Iterator it = null;
            if (o instanceof Iterator) {
                it = (Iterator) o;
            }
            else if (o instanceof Iterable) {
                it = ((Iterable) o).iterator();
            }
            else if (o.getClass().isArray()) {
                it = new Iterator<Object>() {
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
            }
            if (it != null) {
                scope.setBlockValue(it);
                return it.hasNext();
            }
            throw new ShadowExecutionError(block.getLine(), block.argumentIndex(0), "Argument should be an iterator or iterable object.");
        });
        foreach.setEnterCallback(BlockEnterCallback.iterateParameter(0));
        foreach.setEndCallback(BlockEndCallback.iterateParameter(0));
        foreach.setGenerator((c, block, type, method) -> {
            ShadowSection input = block.getArguments().get(0);
            Parameter p = block.getParameters().get(0);
            Class<?> loopType = p.getType();
            Class<?> arg = input.getReturnType(c.getScope());
            if (arg.isArray()) loopType = arg.getComponentType();
            Code iterable = JavaGen.litArg(c, block, 0, type, method);
            if (Iterator.class.isAssignableFrom(input.getReturnType(c.getScope()))) {
                String name = c.getScope().parent().nextTemp();
                method.addStatement("$T $L = $L", arg, name, iterable);
                method.beginControlFlow("while ($L.hasNext())", name);
                CodeBlock.Builder code = CodeBlock.builder();
                code.add("$T $L = ", loopType, p.getName());
                if (loopType != Object.class) {
                    code.add("($T) ", loopType);
                }
                code.add("$L.next()", name);
                method.addStatement(code.build());
                block.addBody(c, type, method);
            }
            else {
                method.beginControlFlow("for ($T $L : $L)", loopType, p.getName(), iterable);
                block.addBody(c, type, method);
            }
            method.endControlFlow();
        });
        context.addBlock(foreach);
    }
    
    @Entity
    void blockWhile() {
        BlockType aWhile = new BlockType("while", new Range.Single(1), new Range.None());
        aWhile.setContextTransformer(ContextTransformer.blockUse(loopControl(aWhile)));
        aWhile.setPreRunCheck((block, scope, args) -> {
            return block.getBoolean(0, scope);
        });
        aWhile.setEndCallback((block, stepper, scope) -> {
            boolean argument = block.getBoolean(0, scope);
            if (argument) stepper.restart();
        });
        aWhile.setGenerator((c, block, type, method) -> {
            method.beginControlFlow("while ($L)", block.getArguments().get(0).getGeneration(c, type, method));
            block.addBody(c, type, method);
            method.endControlFlow();
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
        benchmark.setGenerator((c, block, type, method) -> {
            String b = c.getScope().nextTemp();
            String e = c.getScope().nextTemp();
            String t = c.getScope().nextTemp();
            method.addStatement("long $L = $T.nanoTime()", b, System.class);
            block.addBody(c, type, method);
            method.addStatement("long $L = $T.nanoTime()", e, System.class);
            method.addStatement("double $L = (double) ($L - $L) / 1e6", t, e, b);
            if (block.getArguments().size() == 0) method.addStatement("$T.out.println($L)", System.class, t);
            else {
                Identifier v = block.getIdentifier(0);
                if (c.getScope().isMarked(v.getValidName())) {
                    if (!c.getScope().get(v.getName()).filter(vt -> vt == Double.class).isPresent()) {
                        throw new ShadowParseError(v.getLine(), v.index(), "Variable already defined as non-Double type.");
                    }
                    method.addStatement("$L = $L", v.getName(), t);
                }
                else {
                    c.getScope().mark(v.getName());
                    c.getScope().addCheck(v.getName(), Double.class);
                    method.addStatement("double $L = $L", v.getName(), t);
                }
            }
        });
        context.addBlock(benchmark);
    }
    
    @Entity
    void blockUsing() {
        BlockType using = new BlockType("using", new Range.Single(1), new Range.None());
        using.setParseCallback((block, context1) -> {
            Identifier name = block.getIdentifier(0);
            context1.pokeModule(name.getName());
        });
        using.setContextTransformer(ContextTransformer.blockModule(0));
        using.setGenerator((c, block, type, method) -> {
            method.beginControlFlow("");
            block.addBody(c, type, method);
            method.endControlFlow();
        });
        context.addBlock(using);
    }
    
    //endregion
    
}
