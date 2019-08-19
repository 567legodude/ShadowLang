package com.ssplugins.shadow3.entity;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.JavaComponent;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.execute.Stepper;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.section.ShadowString;
import com.ssplugins.shadow3.util.NumberType;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ShadowEntity implements JavaComponent {
    
    private ShadowContext topContext;
    
    private Flow flow;
    private ShadowEntity previous;
    private ShadowEntity next;
    
    private TokenLine line;
    private int index;
    private ShadowEntity parent;
    
    private ShadowEntity from;
    
    private boolean inline;
    
    private List<Runnable> completeCallbacks = new LinkedList<>();
    
    public ShadowEntity(TokenLine line, ShadowEntity parent) {
        this.line = line;
        this.parent = parent;
        flow = new Flow(this);
    }
    
    public abstract String getName();
    
    public abstract Object execute(Stepper stepper, Scope scope, List<Object> args);
    
    public abstract void addArgument(ShadowSection section);
    
    public abstract List<ShadowSection> getArguments();
    
    public abstract ShadowContext getInnerContext();
    
    protected final void runCompleteCallbacks() {
        for (Runnable callback : completeCallbacks) {
            callback.run();
        }
        completeCallbacks.clear();
        completeCallbacks = null;
    }
    
    public final void addCompleteCallback(Runnable runnable) {
        completeCallbacks.add(runnable);
    }
    
    public ShadowContext getEffectiveContext() {
        ShadowEntity from = getFrom();
        if (from == null) return getTopContext();
        ShadowContext ic = getFrom().getInnerContext();
        if (ic == null) return getFrom().getEffectiveContext();
        return ic;
    }
    
    public <T> T getArgument(int index, Class<T> type, Scope scope, String err) {
        ShadowSection section = getArguments().get(index);
        Object o = section.toObject(scope);
        if (!NumberType.isAssignableFrom(type, o.getClass())) throw ShadowCodeException.sectionExec(section, err).get();
        return type.cast(o);
    }
    
    public <T extends ShadowSection> T getArgumentSection(int index, Class<T> type, String err) {
        ShadowSection section = getArguments().get(index);
        if (!type.isInstance(section)) throw ShadowCodeException.sectionExec(section, err).get();
        return type.cast(section);
    }
    
    public Identifier getIdentifier(int index) {
        return getArgumentSection(index, Identifier.class, "Argument should be an identifier.");
    }
    
    public Number getNumber(int index, Class<? extends Number> type, Scope scope) {
        ShadowSection section = getArguments().get(index);
        Object o = section.toObject(scope);
        NumberType t = NumberType.from(type);
        if (t == null || !t.validValue(o.getClass())) throw ShadowCodeException.sectionExec(section, "Argument must be a number.").get();
        return (Number) o;
    }
    
    public Number getNumber(int index, Scope scope) {
        return getNumber(index, Double.class, scope);
    }
    
    public String getString(int index, Scope scope) {
        return getArgument(index, String.class, scope, "Argument must be a string.");
    }
    
    public ShadowString getStringSection(int index) {
        return getArgumentSection(index, ShadowString.class, "Argument must be a string literal.");
    }
    
    public int getInt(int index, Scope scope) {
        return getArgument(index, Integer.class, scope, "Argument must be an integer.");
    }
    
    public long getLong(int index, Scope scope) {
        return getArgument(index, Long.class, scope, "Argument must be a long.");
    }
    
    public double getDouble(int index, Scope scope) {
        return getArgument(index, Double.class, scope, "Argument must be a double.");
    }
    
    public boolean getBoolean(int index, Scope scope) {
        return getArgument(index, Boolean.class, scope, "Argument must be a boolean.");
    }
    
    public List<Object> argumentValues(Scope scope) {
        return getArguments().stream().map(section -> section.toObject(scope)).collect(Collectors.toList());
    }
    
    public List<Object> argumentValues(Scope scope, int start) {
        return getArguments().stream().skip(start).map(section -> section.toObject(scope)).collect(Collectors.toList());
    }
    
    public Object argumentValue(int index, Scope scope) {
        return getArguments().get(index).toObject(scope);
    }
    
    public int argumentIndex(int index) {
        if (index == -1) return getLine().getTokens().get(getIndex()).getIndex();
        return getArguments().get(index).getPrimaryToken().getIndex();
    }
    
    ShadowEntity getPrevious() {
        return previous;
    }
    
    void setPrevious(ShadowEntity previous) {
        this.previous = previous;
    }
    
    ShadowEntity getNext() {
        return next;
    }
    
    void setNext(ShadowEntity next) {
        this.next = next;
    }
    
    protected void setTopContext(ShadowContext context) {
        this.topContext = context;
    }
    
    public ShadowContext getTopContext() {
        return topContext;
    }
    
    public Flow flow() {
        return flow;
    }
    
    public ShadowEntity getParent() {
        return parent;
    }
    
    public ShadowEntity getFrom() {
        return from;
    }
    
    protected void setFrom(ShadowEntity from) {
        this.from = from;
    }
    
    public TokenLine getLine() {
        return line;
    }
    
    public int getIndex() {
        return index;
    }
    
    protected void setIndex(int index) {
        this.index = index;
    }
    
    public boolean isInline() {
        return inline;
    }
    
    public void setInline(boolean inline) {
        this.inline = inline;
    }
    
}
