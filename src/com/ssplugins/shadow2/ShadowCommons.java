package com.ssplugins.shadow2;

import com.ssplugins.shadow2.def.BlockDef;
import com.ssplugins.shadow2.def.EvalSymbolDef;
import com.ssplugins.shadow2.def.KeywordDef;
import com.ssplugins.shadow2.def.ReplacerDef;

import java.util.List;

public class ShadowCommons extends ShadowAPI {
	
	@Override
	public List<KeywordDef> registerKeywords() {
		return super.registerKeywords();
	}
	
	@Override
	public List<BlockDef> registerBlocks() {
		return super.registerBlocks();
	}
	
	@Override
	public List<ReplacerDef> registerReplacers() {
		return super.registerReplacers();
	}
	
	@Override
	public List<EvalSymbolDef> registerEvalSymbols() {
		return super.registerEvalSymbols();
	}
	
}
