package com.ssplugins.shadow2;

import com.ssplugins.shadow2.Stepper.StepAction;
import com.ssplugins.shadow2.common.ClassFinder;
import com.ssplugins.shadow2.common.NamedReference;
import com.ssplugins.shadow2.common.Range;
import com.ssplugins.shadow2.def.*;
import com.ssplugins.shadow2.element.Plain;
import com.ssplugins.shadow2.element.ShadowElement;
import com.ssplugins.shadow2.element.ShadowSection;
import com.ssplugins.shadow2.exceptions.ShadowExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShadowCommons extends ShadowAPI {
	
	@Override
	public List<ClassFinder> registerClassFinders() {
		List<ClassFinder> out = new ArrayList<>();
		out.add(finderStandard());
		return out;
	}
	
	@Override
	public List<ExpressionDef> registerExpressions() {
		List<ExpressionDef> out = new ArrayList<>();
		out.add(expAdd());
		out.add(expSubtract());
		out.add(expMultiply());
		out.add(expDivide());
		out.add(expMod());
		out.add(expLessThan());
		out.add(expLessThanEqual());
		out.add(expGreaterThan());
		out.add(expGreaterThanEqual());
		out.add(expEquals());
		out.add(expNotEquals());
		out.add(expOr());
		out.add(expAnd());
		return out;
	}
	
	@Override
	public List<KeywordDef> registerKeywords() {
		List<KeywordDef> out = new ArrayList<>();
		out.add(keywordLog());
		out.add(keywordSet());
		out.add(keywordCall());
		return out;
	}
	
	@Override
	public List<BlockDef> registerBlocks() {
		List<BlockDef> out = new ArrayList<>();
		out.add(blockTest());
		out.add(blockIf());
		out.add(blockElse());
		out.add(blockCount());
		return out;
	}
	
	@Override
	public List<ReplacerDef> registerReplacers() {
		List<ReplacerDef> out = new ArrayList<>();
		out.add(replacerToString());
		out.add(replacerEval());
		return out;
	}
	
	@Override
	public List<EvalSymbolDef> registerEvalSymbols() {
		List<EvalSymbolDef> out = new ArrayList<>();
		out.add(evalVar());
		out.add(evalString());
		out.add(evalCast());
		out.add(evalMethod());
		out.add(evalConstruct());
		out.add(evalField());
		out.add(evalParse());
		return out;
	}
	
	private ClassFinder finderStandard() {
		return input -> {
			boolean arr = input.endsWith("[]");
			if (arr) input = input.substring(0, input.length() - 2);
			try {
				Class<?> type = Class.forName(input);
				return Optional.of(type);
			} catch (ClassNotFoundException ignored) {}
			for (Package p : Package.getPackages()) {
				try {
					Class<?> type = Class.forName(arr ? "[L" + p.getName() + "." + input + ";" : p.getName() + "." + input);
					return Optional.of(type);
				} catch (ClassNotFoundException ignored) {}
			}
			return Optional.empty();
		};
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
	
	private ExpressionDef expAnd() {
		return new ExpressionDef("&&", Expressions::and);
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
	
	private BlockDef blockIf() {
		BlockDef def = new BlockDef("if");
		def.setModifierCount(Range.single(1));
		def.setEntryCondition((def1, mods, scope, stepper) -> {
			Optional<Boolean> op = ShadowTools.asBoolean(mods.get(0), scope);
			if (!op.isPresent()) throw new ShadowExecutionException("Modifiers could not be parsed as boolean.");
			return op.get();
		});
		return def;
	}
	
	private BlockDef blockElse() {
		BlockDef def = new BlockDef("else");
		def.setEntryCondition((def1, mods, scope, stepper) -> {
			ShadowElement element = stepper.getLastElement();
			if (element == null ||
					!element.isBlock() ||
					!element.asBlock().getName().equalsIgnoreCase("if")) {
				throw new ShadowExecutionException("Else must be preceded by \"if\" block.");
			}
			return !stepper.lastElementRan();
		});
		return def;
	}
	
	private BlockDef blockCount() {
		BlockDef def = new BlockDef("count");
		def.setModifierCount(Range.from(2, 3));
		def.setParameterCount(Range.single(1));
		def.setSectionParser(SectionParser.allPlain());
		def.setEntryCondition((def1, mods, scope, stepper) -> {
			Optional<Number> start = ShadowTools.asNumber(mods.get(0).asPlain());
			Optional<Number> end = ShadowTools.asNumber(mods.get(1).asPlain());
			if (start.isPresent() && end.isPresent()) {
				if (mods.size() < 3) return true;
				Optional<Plain> step = ShadowTools.get(mods.get(2)).map(ShadowSection::asPlain);
				if (step.flatMap(ShadowTools::asNumber).isPresent()) return true;
			}
			throw new ShadowExecutionException("All parameters must be numbers.");
		});
		def.setEnterEvent((def1, mods, parameters, scope, stepper) -> {
			scope.setParamVar(parameters.get(0), ShadowTools.asNumber(mods.get(0).asPlain()).map(Number::intValue).orElse(0));
		});
		def.setEndEvent((def1, mods, parameters, scope, stepper) -> {
			Optional<NamedReference<Object>> var = scope.getVar(parameters.get(0));
			if (!var.isPresent()) stepper.next(StepAction.BREAK);
			else {
				int start = ShadowTools.asNumber(mods.get(0).asPlain()).map(Number::intValue).orElse(0);
				int end = ShadowTools.asNumber(mods.get(1).asPlain()).map(Number::intValue).orElse(1);
				int step = ShadowTools.get(mods).filter(sections -> sections.size() > 2).map(sections -> sections.get(2)).map(ShadowSection::asPlain).flatMap(ShadowTools::asNumber).map(Number::intValue).orElse(1);
				NamedReference<Object> ref = var.get();
				int n = (int) ref.get();
				if (start < end) {
					ref.set(n + step);
					if (n + step > end) return;
				}
				if (end < start) {
					ref.set(n - step);
					if (n - step < end) return;
				}
				stepper.next(StepAction.RESTART);
			}
		});
		return def;
	}
	
	private ReplacerDef replacerToString() {
		ReplacerDef def = new ReplacerDef("", (sections, scope) -> {
			ShadowTools.verifySections(sections, Range.lowerBound(1));
			String var = sections.get(0).toString();
			Optional<NamedReference<Object>> op = scope.getVar(var);
			if (!op.isPresent()) throw new ShadowExecutionException("Var " + var + " not found in scope.");
			return new Plain(ShadowTools.get(op.get().get()).map(Object::toString).orElse("null"));
		});
		def.setSplitter(Splitter.singleArg());
		def.setSectionParser(SectionParser.allPlain());
		return def;
	}
	
	private ReplacerDef replacerEval() {
		ReplacerDef def = new ReplacerDef("%", (sections, scope) -> {
			ShadowTools.verifySections(sections, Range.lowerBound(1));
			return ShadowTools.executeEval(sections.get(0).asEvalGroup(), scope);
		});
		def.setSplitter(Splitter.evalSplit());
		def.setSectionParser(SectionParser.evalParser());
		return def;
	}
	
	private EvalSymbolDef evalCast() {
		EvalSymbolDef def = new EvalSymbolDef(">", (reference, section, scope) -> {
			Optional<Class<?>> op = scope.getContext().findClass(section.getName());
			if (!op.isPresent()) throw new ShadowExecutionException("Could not find class: " + section.getName(), scope.getContext());
			reference.setType(op.get());
			return reference;
		});
		return def;
	}
	
	private EvalSymbolDef evalMethod() {
		EvalSymbolDef def = new EvalSymbolDef(":", (reference, section, scope) -> {
			Reflect.of(reference, scope).method(section.getName(), section.getParams());
			return reference;
		});
		return def;
	}
	
	private EvalSymbolDef evalString() {
		EvalSymbolDef def = new EvalSymbolDef("?", (reference, section, scope) -> {
			reference.set(section.getName());
			return reference;
		});
		return def;
	}
	
	private EvalSymbolDef evalVar() {
		EvalSymbolDef def = new EvalSymbolDef("<", (reference, section, scope) -> {
			Optional<NamedReference<Object>> var = scope.getVar(section.getName());
			if (!var.isPresent()) throw new ShadowExecutionException("No variable defined named " + section.getName());
			reference.set(var.get().get());
			return reference;
		});
		return def;
	}
	
	private EvalSymbolDef evalConstruct() {
		EvalSymbolDef def = new EvalSymbolDef("=", (reference, section, scope) -> {
			Reflect.of(reference, scope).construct(section.getName(), section.getParams());
			return reference;
		});
		return def;
	}
	
	private EvalSymbolDef evalField() {
		EvalSymbolDef def = new EvalSymbolDef("~", (reference, section, scope) -> {
			Reflect.of(reference, scope).field(section.getName());
			return reference;
		});
		return def;
	}
	
	private EvalSymbolDef evalParse() {
		EvalSymbolDef def = new EvalSymbolDef("-", (reference, section, scope) -> {
			String s = section.getName();
			try {
				if (s.contains(".")) reference.set(Double.parseDouble(s));
				else reference.set(Integer.parseInt(s));
				return reference;
			} catch (NumberFormatException ignored) {}
			if (s.equalsIgnoreCase("true")) reference.set(true);
			else if (s.equalsIgnoreCase("false")) reference.set(false);
			else if (s.equalsIgnoreCase("null")) reference.set(null);
			reference.set(s);
			return reference;
		});
		return def;
	}
	
}
