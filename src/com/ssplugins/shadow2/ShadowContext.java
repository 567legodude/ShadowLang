package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.ParseLevel;
import com.ssplugins.shadow2.common.Parser;
import com.ssplugins.shadow2.def.BlockDef;
import com.ssplugins.shadow2.def.EvalSymbolDef;
import com.ssplugins.shadow2.def.KeywordDef;
import com.ssplugins.shadow2.def.ReplacerDef;
import com.ssplugins.shadow2.exceptions.ShadowAPIException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ShadowContext implements ParseContext {
	
	private int line;
	private String raw;
	private String block;
	private ParseLevel level;
	
	private List<Parser> lineParsers;
	private List<KeywordDef> keywords;
	private List<BlockDef> blocks;
	private List<ReplacerDef> replacers;
	private List<EvalSymbolDef> evalSymbols;
	
	public ShadowContext(List<ShadowAPI> apis, ParseLevel level) {
		if (level == null) level = ParseLevel.NORMAL;
		this.level = level;
		lineParsers = new ArrayList<>();
		KeyedList<KeywordDef> keywords = new KeyedList<>(KeywordDef::getKeyword);
		KeyedList<BlockDef> blocks = new KeyedList<>(BlockDef::getName);
		KeyedList<ReplacerDef> replacers = new KeyedList<>(ReplacerDef::getToken);
		KeyedList<EvalSymbolDef> evalSymbols = new KeyedList<>(EvalSymbolDef::getToken);
		for (ShadowAPI api : apis) {
			ternary(api.registerLineParsers(), lineParsers::addAll);
			ternary(api.registerKeywords(), keywords::addAll);
			ternary(api.registerBlocks(), blocks::addAll);
			ternary(api.registerReplacers(), replacers::addAll);
			ternary(api.registerEvalSymbols(), evalSymbols::addAll);
		}
		checkDuplicates(keywords, s -> "Duplicate keywords registered: " + s);
		checkDuplicates(blocks, s -> "Duplicate blocks registered: " + s);
		checkDuplicates(replacers, s -> "Duplicate replacers registered: " + s);
		checkDuplicates(evalSymbols, s -> "Duplicate eval symbols registered: " + s);
		lineParsers = lockList(lineParsers);
		this.keywords = lockList(keywords);
		this.blocks = lockList(blocks);
		this.replacers = lockList(replacers);
		this.evalSymbols = lockList(evalSymbols);
	}
	
	private <T> void ternary(T value, Consumer<T> consumer) {
		if (value != null) consumer.accept(value);
	}
	
	private <T> void checkDuplicates(KeyedList<T> list, UnaryOperator<String> message) {
		List<String> dupes = list.duplicateKeys();
		if (dupes.size() > 0) throw new ShadowAPIException(message.apply("[" + String.join(",", dupes) + "]"));
	}
	
	private <T> List<T> lockList(List<T> list) {
		return Collections.unmodifiableList(list);
	}
	
	public void nextLine() {
		line++;
	}
	
	public void setRaw(String raw) {
		this.raw = raw;
	}
	
	public void setBlock(String block) {
		this.block = block;
	}
	
	@Override
	public int getLine() {
		return line;
	}
	
	@Override
	public String raw() {
		return raw;
	}
	
	@Override
	public String parentBlock() {
		return block;
	}
	
	@Override
	public ParseLevel getParseLevel() {
		return level;
	}
	
	@Override
	public List<Parser> getLineParsers() {
		return lineParsers;
	}
	
	@Override
	public List<KeywordDef> getKeywords() {
		return keywords;
	}
	
	@Override
	public List<BlockDef> getBlocks() {
		return blocks;
	}
	
	@Override
	public List<ReplacerDef> getReplacers() {
		return replacers;
	}
	
	@Override
	public List<EvalSymbolDef> getEvalSymbols() {
		return evalSymbols;
	}
	
	@Override
	public String toString() {
		return "Line " + line + ": \"" + raw + "\"";
	}
}
