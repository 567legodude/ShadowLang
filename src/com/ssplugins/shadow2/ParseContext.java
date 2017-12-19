package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.ClassFinder;
import com.ssplugins.shadow2.common.ParseLevel;
import com.ssplugins.shadow2.common.Parser;
import com.ssplugins.shadow2.def.BlockDef;
import com.ssplugins.shadow2.def.EvalSymbolDef;
import com.ssplugins.shadow2.def.KeywordDef;
import com.ssplugins.shadow2.def.ReplacerDef;

import java.util.List;
import java.util.Optional;

public interface ParseContext {
	
	int getLine();
	
	String raw();
	
	String parentBlock();
	
	ParseLevel getParseLevel();
	
	List<Parser> getLineParsers();
	
	List<ClassFinder> getClassFinders();
	
	Optional<Class<?>> findClass(String input);
	
	List<KeywordDef> getKeywords();
	
	Optional<KeywordDef> findKeyword(String keyword);
	
	List<BlockDef> getBlocks();
	
	Optional<BlockDef> findBlock(String name);
	
	List<ReplacerDef> getReplacers();
	
	Optional<ReplacerDef> findReplacer(String token);
	
	List<EvalSymbolDef> getEvalSymbols();
	
	Optional<EvalSymbolDef> findEvalSymbol(String token);
	
	static ParseContext empty() {
		return new ParseContext() {
			@Override
			public int getLine() {
				return 0;
			}
			
			@Override
			public String raw() {
				return null;
			}
			
			@Override
			public String parentBlock() {
				return null;
			}
			
			@Override
			public ParseLevel getParseLevel() {
				return null;
			}
			
			@Override
			public List<Parser> getLineParsers() {
				return null;
			}
			
			@Override
			public List<ClassFinder> getClassFinders() {
				return null;
			}
			
			@Override
			public Optional<Class<?>> findClass(String input) {
				return Optional.empty();
			}
			
			@Override
			public List<KeywordDef> getKeywords() {
				return null;
			}
			
			@Override
			public Optional<KeywordDef> findKeyword(String keyword) {
				return Optional.empty();
			}
			
			@Override
			public List<BlockDef> getBlocks() {
				return null;
			}
			
			@Override
			public Optional<BlockDef> findBlock(String name) {
				return Optional.empty();
			}
			
			@Override
			public List<ReplacerDef> getReplacers() {
				return null;
			}
			
			@Override
			public Optional<ReplacerDef> findReplacer(String token) {
				return Optional.empty();
			}
			
			@Override
			public List<EvalSymbolDef> getEvalSymbols() {
				return null;
			}
			
			@Override
			public Optional<EvalSymbolDef> findEvalSymbol(String token) {
				return Optional.empty();
			}
		};
	}
	
}
