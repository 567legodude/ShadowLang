package com.ssplugins.shadow3.api;

import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.def.BlockType;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.OperatorType;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.section.Identifier;
import com.ssplugins.shadow3.section.Operator;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ShadowContext {
    
    private File source;
    private String name;
    private String fileName;
    
    private List<Runnable> triggers = new LinkedList<>();
    
    private Map<String, OperatorMap> operators = new HashMap<>();
    private Map<String, BlockType> blocks = new HashMap<>();
    private Map<String, KeywordType> keywords = new HashMap<>();
    private Map<String, ShadowContext> modules = new HashMap<>();
    private Map<String, FunctionMap> functions = new HashMap<>();
    
    private GenerateContext generateContext;
    
    public ShadowContext() {
        this(null);
    }
    
    public ShadowContext(File source) {
        this.source = source;
    }
    
    public File getSource() {
        return source;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getComponentName(String part) {
        if (name == null) throw new ShadowException("Context has no name defined.");
        return name + "_" + part;
    }
    
    public GenerateContext getGenerateContext() {
        return generateContext;
    }
    
    public void setGenerateContext(GenerateContext generateContext) {
        this.generateContext = generateContext;
    }
    
    private <T> Optional<T> search(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).findFirst();
    }
    
    private <T> T getOrElse(Map<String, T> map, String key, Supplier<T> supplier) {
        return map.computeIfAbsent(key, s -> supplier.get());
    }
    
    public void clean() {
        operators.forEach((s, map) -> map.clean());
        operators.clear();
    }
    
    public void addTrigger(Runnable runnable) {
        triggers.add(runnable);
    }
    
    public void poke() {
        if (triggers.size() == 0) return;
        triggers.forEach(Runnable::run);
        triggers.clear();
    }
    
    //region Operators
    
    private OperatorMap getOpMap(OperatorType action) {
        return operators.computeIfAbsent(action.getToken(), s -> new OperatorMap(action.getOrder(), action.isLeftToRight()));
    }
    
    public boolean addOperator(OperatorType operator) {
        return getOpMap(operator).insert(operator);
    }
    
    public boolean containsOperator(String token) {
        return operators.containsKey(token);
    }
    
    public Optional<OperatorType> findOperator(String token, Object left, Object right) {
        OperatorMap map = operators.get(token);
        if (map == null) return Optional.empty();
        return map.find(left, right);
    }
    
    public Optional<OperatorType<?, ?, ?>> findOperator(String token, Class<?> left, Class<?> right) {
        OperatorMap map = operators.get(token);
        if (map == null) return Optional.empty();
        return map.find(left, right);
    }
    
    public Optional<OperatorMap> getOperatorMap(String token) {
        return Optional.ofNullable(operators.get(token));
    }
    
    public Optional<Operator.OpOrder> getOperatorOrder(String token) {
        return Optional.ofNullable(operators.get(token)).map(OperatorMap::getOrder);
    }
    
    public Optional<Boolean> isOperatorLTR(String token) {
        return Optional.ofNullable(operators.get(token)).map(OperatorMap::isLeftToRight);
    }
    
    public Set<String> operators() {
        return operators.keySet();
    }
    
    //endregion
    //region Blocks
    
    public boolean addBlock(BlockType block) {
        if (blocks.containsKey(block.getName())) return false;
        blocks.put(block.getName(), block);
        return true;
    }
    
    public Optional<BlockType> findBlock(String name) {
        return Optional.ofNullable(blocks.get(name));
    }
    
    //endregion
    //region Keywords
    
    public boolean addKeyword(KeywordType keyword) {
        if (keywords.containsKey(keyword.getName())) return false;
        keywords.put(keyword.getName(), keyword);
        return true;
    }
    
    public Optional<KeywordType> findKeyword(String name) {
        return Optional.ofNullable(keywords.get(name));
    }
    
    //endregion
    //region Modules
    
    public boolean addModule(String name, ShadowContext context) {
        if (modules.containsKey(name)) return false;
        modules.put(name, context);
        return true;
    }
    
    public boolean addLazyModule(String name, ShadowAPI api) {
        if (modules.containsKey(name)) return false;
        ShadowContext context = new ShadowContext();
        context.addTrigger(() -> api.loadInto(context));
        modules.put(name, context);
        return true;
    }
    
    public boolean pokeModule(String name) {
        ShadowContext context = modules.get(name);
        if (context == null) return false;
        context.poke();
        return true;
    }
    
    public Optional<ShadowContext> findModule(String name) {
        return Optional.ofNullable(modules.get(name));
    }
    
    //endregion
    //region Functions
    
    public boolean addFunction(Block block) {
        BlockType define = blocks.get("define");
        if (define == null) throw new IllegalArgumentException("The function definition block has not been added.");
        if (block.getDefinition() != define) throw new IllegalArgumentException("Block is not a function definition.");
        String name = ((Identifier) block.getModifiers().get(0)).getName();
        FunctionMap map = getOrElse(functions, name, FunctionMap::new);
        if (map.contains(block.getParameters().size()))
            throw new IllegalArgumentException("Duplicate function definition.");
        map.set(block.getParameters().size(), block);
        return true;
    }
    
    public Optional<Block> findFunction(String name, int i) {
        return Optional.ofNullable(functions.get(name)).flatMap(fMap -> fMap.get(i));
    }
    
    //endregion
    
}
