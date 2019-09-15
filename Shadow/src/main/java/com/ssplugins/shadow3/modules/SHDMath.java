package com.ssplugins.shadow3.modules;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.JavaGen;
import com.ssplugins.shadow3.compile.KeywordGen;
import com.ssplugins.shadow3.compile.TypeChecker;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.Returnable;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.NumberType;
import com.ssplugins.shadow3.util.Range;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SHDMath extends ShadowAPI {
    
    private ShadowContext context;
    
    @Override
    public void loadInto(ShadowContext context) {
        this.context = context;
        context.setName("math");
        callAnnotatedMethods();
        this.context = null;
    }
    
    //region Helper
    
    private static class NumberKeyword extends KeywordType {
        NumberKeyword(String name, Function<Integer, Integer> iFunc, Function<Double, Double> dFunc, Function<Long, Long> lFunc, Function<Float, Float> fFunc) {
            super(name, new Range.Single(1));
            setAction((keyword, stepper, scope) -> {
                Number n = keyword.getNumber(0, scope);
                if (n instanceof Double) return dFunc.apply(n.doubleValue());
                if (n instanceof Float) return fFunc.apply(n.floatValue());
                if (n instanceof Long) return lFunc.apply(n.longValue());
                return iFunc.apply(n.intValue());
            });
            setReturnable((keyword, scope) -> {
                ShadowSection section = keyword.getArguments().get(0);
                Class<?> type = section.getReturnType(scope);
                TypeChecker.require(section, NumberType.DOUBLE.validValue(type), "Incompatible number type.");
                return NumberType.INT.asMinimum(type);
            });
            setGenerator(singleArgGen(name, NumberType.DOUBLE));
        }
    }
    
    private static class DoubleNumberKeyword extends KeywordType {
        DoubleNumberKeyword(String name, BiFunction<Integer, Integer, Integer> iFunc, BiFunction<Double, Double, Double> dFunc, BiFunction<Long, Long, Long> lFunc, BiFunction<Float, Float, Float> fFunc) {
            super(name, new Range.Single(1));
            setAction((keyword, stepper, scope) -> {
                Number a = keyword.getNumber(0, scope);
                Number b = keyword.getNumber(1, scope);
                if (a instanceof Double || b instanceof Double) return dFunc.apply(a.doubleValue(), b.doubleValue());
                if (a instanceof Float || b instanceof Float) return fFunc.apply(a.floatValue(), b.floatValue());
                if (a instanceof Long || b instanceof Long) return lFunc.apply(a.longValue(), b.longValue());
                return iFunc.apply(a.intValue(), b.intValue());
            });
            setReturnable((keyword, scope) -> {
                List<ShadowSection> args = keyword.getArguments();
                Class<?> a = args.get(0).getReturnType(scope);
                Class<?> b = args.get(1).getReturnType(scope);
                TypeChecker.require(args.get(0), NumberType.DOUBLE.validValue(a), "Incompatible number type.");
                TypeChecker.require(args.get(1), NumberType.DOUBLE.validValue(b), "Incompatible number type.");
                if (a == Double.class || b == Double.class) return Double.class;
                if (a == Float.class || b == Float.class) return Float.class;
                if (a == Long.class || b == Long.class) return Long.class;
                return Integer.class;
            });
            setGenerator(doubleArgGen(name));
        }
    }
    
    private static class SingleMathKeyword extends KeywordType {
        SingleMathKeyword(String name, Function<Double, Double> function) {
            super(name, new Range.Single(1));
            setAction((keyword, stepper, scope) -> {
                double n = keyword.getDouble(0, scope);
                return function.apply(n);
            });
            setReturnable(Returnable.of(Double.class));
            setGenerator(singleArgGen(name, NumberType.DOUBLE));
        }
    }
    
    private static class DoubleMathKeyword extends KeywordType {
        DoubleMathKeyword(String name, BiFunction<Double, Double, Double> function, String genName) {
            super(name, new Range.Single(2));
            setAction((keyword, stepper, scope) -> {
                double a = keyword.getDouble(0, scope);
                double b = keyword.getDouble(1, scope);
                return function.apply(a, b);
            });
            setReturnable(Returnable.of(Double.class));
            setGenerator(doubleArgGen(genName == null ? name : genName));
        }
    }
    
    private static KeywordGen singleArgGen(String name, NumberType t) {
        return (c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            Class<?> returnType = section.getReturnType(c.getScope());
            TypeChecker.require(section, t.validValue(returnType), "Incompatible number type.");
            return Code.format("$T." + name + "($L)", Math.class, JavaGen.litArg(c, keyword, 0, type, method));
        };
    }
    
    private static KeywordGen doubleArgGen(String name) {
        return (c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            Class<?> a = args.get(0).getReturnType(c.getScope());
            Class<?> b = args.get(1).getReturnType(c.getScope());
            TypeChecker.require(args.get(0), NumberType.DOUBLE.validValue(a), "Incompatible number type.");
            TypeChecker.require(args.get(1), NumberType.DOUBLE.validValue(b), "Incompatible number type.");
            return Code.format("$T." + name + "($L, $L)", Math.class, JavaGen.litArg(c, keyword, 0, type, method), JavaGen.litArg(c, keyword, 1, type, method));
        };
    }
    
    private static Returnable singleReturnable() {
        return (keyword, scope) -> {
            ShadowSection section = keyword.getArguments().get(0);
            Class<?> returnType = section.getReturnType(scope);
            TypeChecker.require(section, NumberType.DOUBLE.validValue(returnType), "Incompatible number type.");
            if (returnType == Double.class) return (Class<?>) Double.class;
            return Float.class;
        };
    }
    
    private static Returnable doubleReturnable() {
        return (keyword, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            Class<?> a = args.get(0).getReturnType(scope);
            Class<?> b = args.get(1).getReturnType(scope);
            TypeChecker.require(args.get(0), NumberType.LONG.validValue(a), "Incompatible number type.");
            TypeChecker.require(args.get(1), NumberType.LONG.validValue(b), "Incompatible number type.");
            if (a == Long.class || b == Long.class) return (Class<?>) Long.class;
            return Integer.class;
        };
    }
    
    private void singleIntArg(String name, Function<Integer, Integer> iFunc, Function<Long, Long> lFunc, String genName) {
        KeywordType type = new KeywordType(name, new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Number n = keyword.getNumber(0, Long.class, scope);
            if (n instanceof Long) return lFunc.apply(n.longValue());
            return iFunc.apply(n.intValue());
        });
        type.setReturnable((keyword, scope) -> {
            ShadowSection section = keyword.getArguments().get(0);
            Class<?> returnType = section.getReturnType(scope);
            TypeChecker.require(section, NumberType.LONG.validValue(returnType), "Incompatible number type.");
            if (returnType == Long.class) return Long.class;
            return Integer.class;
        });
        type.setGenerator(singleArgGen(genName == null ? name : genName, NumberType.LONG));
        context.addKeyword(type);
    }
    
    private void singleFPArg(String name, Function<Double, Object> dFunc, Function<Float, Object> fFunc, String genName) {
        KeywordType type = new KeywordType(name, new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Number n = keyword.getNumber(0, Double.class, scope);
            if (n instanceof Double) return dFunc.apply(n.doubleValue());
            return fFunc.apply(n.floatValue());
        });
        type.setReturnable(singleReturnable());
        type.setGenerator(singleArgGen(genName == null ? name : genName, NumberType.DOUBLE));
    }
    
    private void doubleIntArg(String name, BiFunction<Integer, Integer, Integer> iFunc, BiFunction<Long, Long, Long> lFunc, String genName) {
        KeywordType type = new KeywordType(name, new Range.Single(2));
        type.setAction((keyword, stepper, scope) -> {
            Number a = keyword.getNumber(0, Long.class, scope);
            Number b = keyword.getNumber(1, Long.class, scope);
            if (a instanceof Long || b instanceof Long) return lFunc.apply(a.longValue(), b.longValue());
            return iFunc.apply(a.intValue(), b.intValue());
        });
        type.setReturnable(doubleReturnable());
        type.setGenerator(doubleArgGen(genName == null ? name : genName));
        context.addKeyword(type);
    }
    
    //endregion
    //region Constants
    
    @Entity
    void constantE() {
        KeywordType e = new KeywordType("E", new Range.None());
        e.setAction((keyword, stepper, scope) -> Math.E);
        e.setReturnable(Returnable.of(Double.class));
        e.setGenerator((c, keyword, type, method) -> Code.format("$T.E", Math.class));
        context.addKeyword(e);
    }
    
    @Entity
    void constantPI() {
        KeywordType pi = new KeywordType("PI", new Range.None());
        pi.setAction((keyword, stepper, scope) -> Math.PI);
        pi.setReturnable(Returnable.of(Double.class));
        pi.setGenerator((c, keyword, type, method) -> Code.format("$T.PI", Math.class));
        context.addKeyword(pi);
    }
    
    //endregion
    //region Functions
    
    @Entity
    void keywordAbs() {
        NumberKeyword abs = new NumberKeyword("abs", Math::abs, Math::abs, Math::abs, Math::abs);
        context.addKeyword(abs);
    }
    
    @Entity
    void keywordCbrt() {
        SingleMathKeyword cbrt = new SingleMathKeyword("cbrt", Math::cbrt);
        context.addKeyword(cbrt);
    }
    
    @Entity
    void keywordCiel() {
        SingleMathKeyword ceil = new SingleMathKeyword("ceil", Math::ceil);
        context.addKeyword(ceil);
    }
    
    @Entity
    void keywordExp() {
        SingleMathKeyword exp = new SingleMathKeyword("exp", Math::exp);
        context.addKeyword(exp);
    }
    
    @Entity
    void keywordExpm1() {
        SingleMathKeyword expm1 = new SingleMathKeyword("expm1", Math::expm1);
        context.addKeyword(expm1);
    }
    
    @Entity
    void keywordFloor() {
        SingleMathKeyword floor = new SingleMathKeyword("floor", Math::floor);
        context.addKeyword(floor);
    }
    
    @Entity
    void keywordHypot() {
        DoubleMathKeyword hypot = new DoubleMathKeyword("hypot", Math::hypot, null);
        context.addKeyword(hypot);
    }
    
    @Entity
    void keywordLog() {
        SingleMathKeyword log = new SingleMathKeyword("log", Math::log);
        context.addKeyword(log);
    }
    
    @Entity
    void keywordLog10() {
        SingleMathKeyword log10 = new SingleMathKeyword("log10", Math::log10);
        context.addKeyword(log10);
    }
    
    @Entity
    void keywordLog1p() {
        SingleMathKeyword log1p = new SingleMathKeyword("log1p", Math::log1p);
        context.addKeyword(log1p);
    }
    
    @Entity
    void keywordMax() {
        DoubleNumberKeyword max = new DoubleNumberKeyword("max", Math::max, Math::max, Math::max, Math::max);
        context.addKeyword(max);
    }
    
    @Entity
    void keywordMin() {
        DoubleNumberKeyword min = new DoubleNumberKeyword("min", Math::min, Math::min, Math::min, Math::min);
        context.addKeyword(min);
    }
    
    @Entity
    void keywordPow() {
        DoubleMathKeyword pow = new DoubleMathKeyword("pow", Math::pow, null);
        context.addKeyword(pow);
    }
    
    @Entity
    void keywordRound() {
        singleFPArg("round", Math::round, Math::round, null);
    }
    
    @Entity
    void keywordSignum() {
        singleFPArg("signum", Math::signum, Math::signum, null);
    }
    
    @Entity
    void keywordSqrt() {
        SingleMathKeyword sqrt = new SingleMathKeyword("sqrt", Math::sqrt);
        context.addKeyword(sqrt);
    }
    
    @Entity
    void keywordDegrees() {
        SingleMathKeyword degrees = new SingleMathKeyword("degrees", Math::toDegrees);
        context.addKeyword(degrees);
    }
    
    @Entity
    void keywordRadians() {
        SingleMathKeyword radians = new SingleMathKeyword("radians", Math::toRadians);
        context.addKeyword(radians);
    }
    
    //endregion
    //region Trig
    
    @Entity
    void keywordAcos() {
        SingleMathKeyword acos = new SingleMathKeyword("acos", Math::acos);
        context.addKeyword(acos);
    }
    
    @Entity
    void keywordAsin() {
        SingleMathKeyword asin = new SingleMathKeyword("asin", Math::asin);
        context.addKeyword(asin);
    }
    
    @Entity
    void keywordAtan() {
        SingleMathKeyword atan = new SingleMathKeyword("atan", Math::atan);
        context.addKeyword(atan);
    }
    
    @Entity
    void keywordAtan2() {
        DoubleMathKeyword atan2 = new DoubleMathKeyword("atan2", Math::atan2, null);
        context.addKeyword(atan2);
    }
    
    @Entity
    void keywordCos() {
        SingleMathKeyword cos = new SingleMathKeyword("cos", Math::cos);
        context.addKeyword(cos);
    }
    
    @Entity
    void keywordCosh() {
        SingleMathKeyword cosh = new SingleMathKeyword("cosh", Math::cosh);
        context.addKeyword(cosh);
    }
    
    @Entity
    void keywordSin() {
        SingleMathKeyword sin = new SingleMathKeyword("sin", Math::sin);
        context.addKeyword(sin);
    }
    
    @Entity
    void keywordSinh() {
        SingleMathKeyword sinh = new SingleMathKeyword("sinh", Math::sinh);
        context.addKeyword(sinh);
    }
    
    @Entity
    void keywordTan() {
        SingleMathKeyword tan = new SingleMathKeyword("tan", Math::tan);
        context.addKeyword(tan);
    }
    
    @Entity
    void keywordTanh() {
        SingleMathKeyword tanh = new SingleMathKeyword("tanh", Math::tanh);
        context.addKeyword(tanh);
    }
    
    //endregion
    //region Special
    
    @Entity
    void keywordAddExact() {
        doubleIntArg("add_exact", Math::addExact, Math::addExact, "addExact");
    }
    
    @Entity
    void keywordCopySign() {
        DoubleMathKeyword copySign = new DoubleMathKeyword("copy_sign", Math::copySign, "copySign");
        context.addKeyword(copySign);
    }
    
    @Entity
    void keywordDecrementExact() {
        singleIntArg("decrement_exact", Math::decrementExact, Math::decrementExact, "decrementExact");
    }
    
    @Entity
    void keywordFloorDiv() {
        doubleIntArg("floor_div", Math::floorDiv, Math::floorDiv, "floorDiv");
    }
    
    @Entity
    void keywordFloorMod() {
        doubleIntArg("floor_mod", Math::floorMod, Math::floorMod, "floorMod");
    }
    
    @Entity
    void keywordGetExponent() {
        singleFPArg("get_exp", Math::getExponent, Math::getExponent, "getExponent");
    }
    
    @Entity
    void keywordIEEERemainder() {
        DoubleMathKeyword ieeeRemainder = new DoubleMathKeyword("ieee_remainder", Math::IEEEremainder, "IEEEremainder");
        context.addKeyword(ieeeRemainder);
    }
    
    @Entity
    void keywordIncrementExact() {
        singleIntArg("increment_exact", Math::incrementExact, Math::incrementExact, "incrementExact");
    }
    
    @Entity
    void keywordMultiplyExact() {
        doubleIntArg("multiply_exact", Math::multiplyExact, Math::multiplyExact, "multiplyExact");
    }
    
    @Entity
    void keywordNegateExact() {
        singleIntArg("negate_exact", Math::negateExact, Math::negateExact, "negateExact");
    }
    
    @Entity
    void keywordNextAfter() {
        KeywordType nextAfter = new KeywordType("next_after", new Range.Single(2));
        nextAfter.setAction((keyword, stepper, scope) -> {
            Number a = keyword.getNumber(0, scope);
            Number b = keyword.getNumber(1, scope);
            if (a instanceof Double) return Math.nextAfter(a.doubleValue(), b.doubleValue());
            return Math.nextAfter(a.floatValue(), b.doubleValue());
        });
        nextAfter.setReturnable(singleReturnable());
        nextAfter.setGenerator(doubleArgGen("nextAfter"));
        context.addKeyword(nextAfter);
    }
    
    @Entity
    void keywordNextDown() {
        singleFPArg("next_down", Math::nextDown, Math::nextDown, "nextDown");
    }
    
    @Entity
    void keywordNextUp() {
        singleFPArg("next_up", Math::nextUp, Math::nextUp, "nextUp");
    }
    
    @Entity
    void keywordRint() {
        SingleMathKeyword rint = new SingleMathKeyword("rint", Math::rint);
        context.addKeyword(rint);
    }
    
    @Entity
    void keywordScalb() {
        KeywordType scalb = new KeywordType("scalb", new Range.Single(2));
        scalb.setAction((keyword, stepper, scope) -> {
            Number a = keyword.getNumber(0, scope);
            Number b = keyword.getNumber(1, scope);
            if (a instanceof Double) return Math.scalb(a.doubleValue(), b.intValue());
            return Math.scalb(a.floatValue(), b.intValue());
        });
        scalb.setReturnable(singleReturnable());
        scalb.setGenerator(doubleArgGen("scalb"));
        context.addKeyword(scalb);
    }
    
    @Entity
    void keywordSubtractExact() {
        doubleIntArg("subtract_exact", Math::subtractExact, Math::subtractExact, "subtractExact");
    }
    
    @Entity
    void keywordToIntExact() {
        KeywordType toIntExact = new KeywordType("to_int_exact", new Range.Single(1));
        toIntExact.setAction((keyword, stepper, scope) -> {
            long n = keyword.getLong(0, scope);
            return Math.toIntExact(n);
        });
        toIntExact.setReturnable(Returnable.of(Integer.class));
        toIntExact.setGenerator(singleArgGen("toIntExact", NumberType.LONG));
        context.addKeyword(toIntExact);
    }
    
    @Entity
    void keywordUlp() {
        singleFPArg("ulp", Math::ulp, Math::ulp, null);
    }
    
    //endregion
    
}
