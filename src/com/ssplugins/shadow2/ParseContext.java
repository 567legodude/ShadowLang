package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.ParseLevel;
import com.ssplugins.shadow2.common.Parser;
import com.ssplugins.shadow2.def.BlockDef;
import com.ssplugins.shadow2.def.EvalSymbolDef;
import com.ssplugins.shadow2.def.KeywordDef;
import com.ssplugins.shadow2.def.ReplacerDef;

import java.util.List;

public interface ParseContext {
	
	int getLine();
	
	String raw();
	
	String parentBlock();
	
	ParseLevel getParseLevel();
	
	List<Parser> getLineParsers();
	
	List<KeywordDef> getKeywords();
	
	List<BlockDef> getBlocks();
	
	List<ReplacerDef> getReplacers();
	
	List<EvalSymbolDef> getEvalSymbols();
	
}
