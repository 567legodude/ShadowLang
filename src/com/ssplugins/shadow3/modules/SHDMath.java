package com.ssplugins.shadow3.modules;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.util.Range;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SHDMath extends ShadowAPI {
    
    private ShadowContext context;
    
    @Override
    public void loadInto(ShadowContext context) {
        this.context = context;
        callAnnotatedMethods();
        this.context = null;
    }
    
    //region Helper
    
    private static class NumberKeyword extends KeywordType {
        public NumberKeyword(String name, Function<Integer, Object> iFunc, Function<Double, Object> dFunc, Function<Long, Object> lFunc, Function<Float, Object> fFunc) {
            super(name, new Range.Single(1));
            setAction((keyword, stepper, scope) -> {
                Number n = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
                if (n instanceof Integer) return iFunc.apply(n.intValue());
                if (n instanceof Double) return dFunc.apply(n.doubleValue());
                if (n instanceof Long) return lFunc.apply(n.longValue());
                if (n instanceof Float) return fFunc.apply(n.floatValue());
                throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "Unknown number type.");
            });
        }
    }
    
    private static class DoubleNumberKeyword extends KeywordType {
        public DoubleNumberKeyword(String name, BiFunction<Integer, Integer, Object> iFunc, BiFunction<Double, Double, Object> dFunc, BiFunction<Long, Long, Object> lFunc, BiFunction<Float, Float, Object> fFunc) {
            super(name, new Range.Single(1));
            setAction((keyword, stepper, scope) -> {
                Number a = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
                Number b = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
                if (a instanceof Double || b instanceof Double) return dFunc.apply(a.doubleValue(), b.doubleValue());
                if (a instanceof Float || b instanceof Float) return fFunc.apply(a.floatValue(), b.floatValue());
                if (a instanceof Long || b instanceof Long) return lFunc.apply(a.longValue(), b.longValue());
                return iFunc.apply(a.intValue(), b.intValue());
            });
        }
    }
    
    private static class SingleMathKeyword extends KeywordType {
        public SingleMathKeyword(String name, Function<Double, Object> function) {
            super(name, new Range.Single(1));
            setAction((keyword, stepper, scope) -> {
                Number n = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
                return function.apply(n.doubleValue());
            });
        }
    }
    
    private static class DoubleMathKeyword extends KeywordType {
        public DoubleMathKeyword(String name, BiFunction<Double, Double, Object> function) {
            super(name, new Range.Single(2));
            setAction((keyword, stepper, scope) -> {
                Number a = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
                Number b = keyword.getArgument(1, Number.class, scope, "Argument must be a number.");
                return function.apply(a.doubleValue(), b.doubleValue());
            });
        }
    }
    
    private void singleIntArg(String name, Function<Integer, Object> iFunc, Function<Long, Object> lFunc) {
        KeywordType type = new KeywordType(name, new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Number n = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
            if (n instanceof Long) return lFunc.apply(n.longValue());
            if (n instanceof Integer) return iFunc.apply(n.intValue());
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "Argument must be int or long.");
        });
        context.addKeyword(type);
    }
    
    private void singleFPArg(String name, Function<Double, Object> dFunc, Function<Float, Object> fFunc) {
        KeywordType type = new KeywordType(name, new Range.Single(1));
        type.setAction((keyword, stepper, scope) -> {
            Number n = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
            if (n instanceof Double) return dFunc.apply(n.doubleValue());
            if (n instanceof Float) return fFunc.apply(n.floatValue());
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "Argument must be float or double.");
        });
        context.addKeyword(type);
    }
    
    private void doubleIntArg(String name, BiFunction<Integer, Integer, Object> iFunc, BiFunction<Long, Long, Object> lFunc) {
        KeywordType type = new KeywordType(name, new Range.Single(2));
        type.setAction((keyword, stepper, scope) -> {
            Number a = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
            Number b = keyword.getArgument(1, Number.class, scope, "Argument must be a number.");
            if (a instanceof Long || b instanceof Long) return lFunc.apply(a.longValue(), b.longValue());
            if (a instanceof Integer && b instanceof Integer) return iFunc.apply(a.intValue(), b.intValue());
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "Arguments must be int or long.");
        });
        context.addKeyword(type);
    }
    
    //endregion
    //region Constants
    
    @Entity
    void constantE() {
        KeywordType e = new KeywordType("E", new Range.None());
        e.setAction((keyword, stepper, scope) -> Math.E);
        context.addKeyword(e);
    }
    
    @Entity
    void constantPI() {
        KeywordType pi = new KeywordType("PI", new Range.None());
        pi.setAction((keyword, stepper, scope) -> Math.PI);
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
        DoubleMathKeyword hypot = new DoubleMathKeyword("hypot", Math::hypot);
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
        DoubleMathKeyword pow = new DoubleMathKeyword("pow", Math::pow);
        context.addKeyword(pow);
    }
    
    @Entity
    void keywordRound() {
        singleFPArg("round", Math::round, Math::round);
    }
    
    @Entity
    void keywordSignum() {
        singleFPArg("signum", Math::signum, Math::signum);
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
        DoubleMathKeyword atan2 = new DoubleMathKeyword("atan2", Math::atan2);
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
        doubleIntArg("add_exact", Math::addExact, Math::addExact);
    }
    
    @Entity
    void keywordCopySign() {
        KeywordType copySign = new KeywordType("copy_sign", new Range.Single(2));
        copySign.setAction((keyword, stepper, scope) -> {
            Number a = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
            Number b = keyword.getArgument(1, Number.class, scope, "Argument must be a number.");
            if (a instanceof Float || b instanceof Float) return Math.copySign(a.floatValue(), b.floatValue());
            if (a instanceof Double && b instanceof Double) return Math.copySign(a.doubleValue(), b.doubleValue());
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "Arguments must be float or double.");
        });
        context.addKeyword(copySign);
    }
    
    @Entity
    void keywordDecrementExact() {
        singleIntArg("decrement_exact", Math::decrementExact, Math::decrementExact);
    }
    
    @Entity
    void keywordFloorDiv() {
        doubleIntArg("floor_div", Math::floorDiv, Math::floorDiv);
    }
    
    @Entity
    void keywordFloorMod() {
        doubleIntArg("floor_mod", Math::floorMod, Math::floorMod);
    }
    
    @Entity
    void keywordGetExponent() {
        singleFPArg("get_exp", Math::getExponent, Math::getExponent);
    }
    
    @Entity
    void keywordIEEERemainder() {
        DoubleMathKeyword ieeeRemainder = new DoubleMathKeyword("ieee_remainder", Math::IEEEremainder);
        context.addKeyword(ieeeRemainder);
    }
    
    @Entity
    void keywordIncrementExact() {
        singleIntArg("increment_exact", Math::incrementExact, Math::incrementExact);
    }
    
    @Entity
    void keywordMultiplyExact() {
        doubleIntArg("multiply_exact", Math::multiplyExact, Math::multiplyExact);
    }
    
    @Entity
    void keywordNegateExact() {
        singleIntArg("negate_exact", Math::negateExact, Math::negateExact);
    }
    
    @Entity
    void keywordNextAfter() {
        KeywordType nextAfter = new KeywordType("next_after", new Range.Single(2));
        nextAfter.setAction((keyword, stepper, scope) -> {
            Number a = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
            Number b = keyword.getArgument(1, Double.class, scope, "Argument must be a number.");
            if (a instanceof Float) return Math.nextAfter(a.floatValue(), b.doubleValue());
            if (a instanceof Double) return Math.nextAfter(a.doubleValue(), b.doubleValue());
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "Argument must be float or double.");
        });
        context.addKeyword(nextAfter);
    }
    
    @Entity
    void keywordNextDown() {
        singleFPArg("next_down", Math::nextDown, Math::nextDown);
    }
    
    @Entity
    void keywordNextUp() {
        singleFPArg("next_up", Math::nextUp, Math::nextUp);
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
            Number a = keyword.getArgument(0, Number.class, scope, "Argument must be a number.");
            Number b = keyword.getArgument(1, Integer.class, scope, "Argument must be an integer.");
            if (a instanceof Float) return Math.scalb(a.floatValue(), b.intValue());
            if (a instanceof Double) return Math.scalb(a.doubleValue(), b.intValue());
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "First argument must be float or double.");
        });
        context.addKeyword(scalb);
    }
    
    @Entity
    void keywordSubtractExact() {
        doubleIntArg("subtract_exact", Math::subtractExact, Math::subtractExact);
    }
    
    @Entity
    void keywordToIntExact() {
        KeywordType toIntExact = new KeywordType("to_int_exact", new Range.Single(1));
        toIntExact.setAction((keyword, stepper, scope) -> {
            Long n = keyword.getArgument(0, Long.class, scope, "Argument must be a long.");
            return Math.toIntExact(n);
        });
        context.addKeyword(toIntExact);
    }
    
    @Entity
    void keywordUlp() {
        singleFPArg("ulp", Math::ulp, Math::ulp);
    }
    
    //endregion
    
}
