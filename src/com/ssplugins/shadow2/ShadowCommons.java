package com.ssplugins.shadow2;

import com.ssplugins.shadow2.Stepper.StepAction;
import com.ssplugins.shadow2.common.Range;
import com.ssplugins.shadow2.def.*;
import com.ssplugins.shadow2.element.Plain;
import com.ssplugins.shadow2.element.ShadowSection;

import java.util.ArrayList;
import java.util.List;

public class ShadowCommons extends ShadowAPI {
	
	@Override
	public List<ExpressionDef> registerExpressions() {
		List<ExpressionDef> out = new ArrayList<>();
		out.add(expAdd());
		out.add(expSubtract());
		return out;
	}
	
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
	
	private ExpressionDef expAdd() {
		return new ExpressionDef("+", Expressions::add);
	}
	
	private ExpressionDef expSubtract() {
		return new ExpressionDef("-", Expressions::subtract);
	}
	
	private ExpressionDef expMultiply() {
		return new ExpressionDef("*", Expressions::multiply);
	}
	
	private ExpressionDef expDivide() {
		return new ExpressionDef("/", Expressions::divide);
	}
	
	private ExpressionDef expMod() {
		return new ExpressionDef("%", Expressions::mod);
	}
	
	private ExpressionDef expLessThan() {
		return new ExpressionDef("<", Expressions::lessThan);
	}
	
	private ExpressionDef expLessThanEqual() {
		return new ExpressionDef("<=", Expressions::lessThanEqual);
	}
	
	private ExpressionDef expGreaterThan() {
		return new ExpressionDef(">", Expressions::greaterThan);
	}
	
	private ExpressionDef expGreaterThanEqual() {
		return new ExpressionDef(">=", Expressions::greaterThanEqual);
	}
	
	private ExpressionDef expEquals() {
		return new ExpressionDef("==", Expressions::equals);
	}
	
	private ExpressionDef expNotEquals() {
		return new ExpressionDef("!=", Expressions::notEquals);
	}
	
	private ExpressionDef expOr() {
		return new ExpressionDef("||", Expressions::or);
	}
	
	private KeywordDef keywordLog() {
		KeywordDef def = new KeywordDef("log", (def1, args, scope, stepper) -> {
			System.out.println(ShadowTools.sectionsToString(args, scope));
		});
		return def;
	}
	
	private KeywordDef keywordSet() {
		KeywordDef def = new KeywordDef("set", (def1, args, scope, stepper) -> {
			String name = args.get(0).asPlain().getValue();
			scope.setVar(name, ShadowTools.executeEval(args.get(1).asEvalGroup(), scope).asReference().getValue());
		});
		def.setArgumentCount(Range.lowerBound(2));
		def.setSplitter((content, context) -> {
			int i = content.indexOf(' ');
			if (i == -1) return new String[0];
			return new String[] {content.substring(0, i), content.substring(i + 1)};
		});
		def.setSectionParser((sections, context) -> {
			ShadowTools.verifyArgs(sections, 2, context);
			List<ShadowSection> out = new ArrayList<>();
			out.add(new Plain(sections[0]));
			out.add(SectionParser.parseAsEval(sections[1], context));
			return out;
		});
		return def;
	}
	
	private KeywordDef keywordUnset() {
		KeywordDef def = new KeywordDef("unset", (def1, args, scope, stepper) -> {
			args.forEach(section -> scope.unset(section.asPlain().getValue()));
		});
		def.setArgumentCount(Range.lowerBound(1));
		def.setSectionParser(SectionParser.allPlain());
		return def;
	}
	
	private KeywordDef keywordBreak() {
		KeywordDef def = new KeywordDef("break", (def1, args, scope, stepper) -> {
			stepper.next(StepAction.BREAK);
		});
		return def;
	}
	
	private KeywordDef keywordBreakAll() {
		KeywordDef def = new KeywordDef("breakall", (def1, args, scope, stepper) -> {
			stepper.next(StepAction.BREAK_ALL);
		});
		return def;
	}
	
	private KeywordDef keywordCall() {
		KeywordDef def = new KeywordDef("call", (def1, args, scope, stepper) -> {
			ShadowTools.executeEval(args.get(0).asEvalGroup(), scope);
		});
		def.setArgumentCount(Range.lowerBound(1));
		def.setSplitter(Splitter.evalSplit());
		def.setSectionParser(SectionParser.evalParser());
		return def;
	}
	
	private KeywordDef keywordRepeatIf() {
		KeywordDef def = new KeywordDef("repeatif", (def1, args, scope, stepper) -> {
			//
		});
		// TODO
		return def;
	}
	
	private BlockDef blockTest() {
		BlockDef def = new BlockDef("test");
		return def;
	}
	
}
