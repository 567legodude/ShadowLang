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
	
}
