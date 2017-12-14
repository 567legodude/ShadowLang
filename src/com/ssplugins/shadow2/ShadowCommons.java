package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.Range;
import com.ssplugins.shadow2.def.*;
import com.ssplugins.shadow2.element.Plain;

import java.util.ArrayList;
import java.util.List;

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
		return out;
	}
	
	@Override
	public List<ReplacerDef> registerReplacers() {
		List<ReplacerDef> out = new ArrayList<>();
		out.add(replacerEval());
		return out;
	}
	
	@Override
	public List<EvalSymbolDef> registerEvalSymbols() {
		List<EvalSymbolDef> out = new ArrayList<>();
		out.add(evalMethod());
		return out;
	}
	
	private KeywordDef keywordLog() {
		KeywordDef def = new KeywordDef("log", (def1, args, scope, stepper) -> {
			System.out.println(ShadowTools.asString(args));
		});
		def.setArgumentCount(Range.lowerBound(1));
		return def;
	}
	
	private ReplacerDef replacerEval() {
		ReplacerDef def = new ReplacerDef("e", (sections, scope) -> new Plain(""));
		def.setSplitter(Splitter.evalSplit());
		def.setSectionParser(SectionParser.evalParser());
		return def;
	}
	
	private EvalSymbolDef evalMethod() {
		EvalSymbolDef def = new EvalSymbolDef(":", reference -> reference);
		return def;
	}
	
}
