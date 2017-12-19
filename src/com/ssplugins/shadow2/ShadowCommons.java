package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.Range;
import com.ssplugins.shadow2.def.*;
import com.ssplugins.shadow2.element.Plain;
import com.ssplugins.shadow2.element.Reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ShadowCommons extends ShadowAPI {
	
	@Override
	public List<KeywordDef> registerKeywords() {
		List<KeywordDef> out = new ArrayList<>();
		out.add(keywordLog());
		return out;
	}
	
	@Override
	public List<BlockDef> registerBlocks() {
		List<BlockDef> out = new ArrayList<>();
		out.add(blockTest());
		return out;
	}
	
	@Override
	public List<ReplacerDef> registerReplacers() {
		List<ReplacerDef> out = new ArrayList<>();
		return out;
	}
	
	@Override
	public List<EvalSymbolDef> registerEvalSymbols() {
		List<EvalSymbolDef> out = new ArrayList<>();
		return out;
	}
	
	private KeywordDef keywordLog() {
		KeywordDef def = new KeywordDef("log", (def1, args, scope, stepper) -> {
			System.out.println(ShadowTools.sectionsToString(args, scope));
		});
		return def;
	}
	
	private BlockDef blockTest() {
		BlockDef def = new BlockDef("test");
		return def;
	}
	
}
