package com.ssplugins.shadow3.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Range {

    protected int[] values;
    
    public Range() {
        this(new int[0]);
    }
    
    public Range(int[] values) {
        this.values = values;
    }
    
    public abstract boolean contains(int value);
    
    public abstract String amount();
    
    public boolean needsS() {
        return true;
    }
    
    public String toString(String entity) {
        return amount() + " " + entity + (needsS() ? "s" : "");
    }
    
    //region Implementations
    
    public static class Any extends Range {
        @Override
        public boolean contains(int value) {
            return true;
        }
    
        @Override
        public String amount() {
            return "any number of";
        }
    }
    
    public static class MinMax extends Range {
        public MinMax(int min, int max) {
            super(new int[] {min, max});
            if (min > max) throw new IllegalArgumentException("Minimum is greater than maximum bound.");
        }
    
        @Override
        public boolean contains(int value) {
            return values[0] <= value && value <= values[1];
        }
    
        @Override
        public String amount() {
            return values[0] + "-" + values[1];
        }
    
        @Override
        public boolean needsS() {
            return values[0] != 1 || values[1] != 1;
        }
    }
    
    public static class Single extends Range {
        public Single(int value) {
            super(new int[] {value});
        }
    
        @Override
        public boolean contains(int value) {
            return values[0] == value;
        }
    
        @Override
        public String amount() {
            return String.valueOf(values[0]);
        }
    
        @Override
        public boolean needsS() {
            return values[0] != 1;
        }
    
    }
    
    public static class Outside extends Range {
        public Outside(int min, int max) {
            super(new int[] {min, max});
        }
    
        @Override
        public boolean contains(int value) {
            return value < values[0] || value > values[1];
        }
    
        @Override
        public String amount() {
            return "<" + values[0] + ",>" + values[1];
        }
    }
    
    public static class LowerBound extends Range {
        public LowerBound(int value) {
            super(new int[] {value});
        }
    
        @Override
        public boolean contains(int value) {
            return value >= values[0];
        }
    
        @Override
        public String amount() {
            return ">=" + values[0];
        }
    }
    
    public static class UpperBound extends Range {
        public UpperBound(int value) {
            super(new int[] {value});
        }
        
        @Override
        public boolean contains(int value) {
            return value <= values[0];
        }
    
        @Override
        public String amount() {
            return "<=" + values[0];
        }
    }
    
    public static class Empty extends Range {
        @Override
        public boolean contains(int value) {
            return false;
        }
    
        @Override
        public String amount() {
            return "N/A";
        }
    }
    
    public static class None extends Range {
        @Override
        public boolean contains(int value) {
            return value == 0;
        }
    
        @Override
        public String amount() {
            return "no";
        }
    
    }
    
    public static class Of extends Range {
        public Of(int... values) {
            super(values);
        }
    
        @Override
        public boolean contains(int value) {
            for (int i : values) {
                if (i == value) return true;
            }
            return false;
        }
    
        @Override
        public String amount() {
            return Arrays.stream(values).mapToObj(Objects::toString).collect(Collectors.joining(", "));
        }
    }
    
    //endregion
    
}
