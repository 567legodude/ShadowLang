package com.ssplugins.shadow;

import com.ssplugins.shadow.common.NamedReference;
import com.ssplugins.shadow.common.Range;
import com.ssplugins.shadow.common.TypeReference;
import com.ssplugins.shadow.def.EvalSymbolDef;
import com.ssplugins.shadow.def.ExpressionDef;
import com.ssplugins.shadow.def.ReplacerDef;
import com.ssplugins.shadow.element.*;
import com.ssplugins.shadow.exceptions.ShadowException;
import com.ssplugins.shadow.exceptions.ShadowExecutionException;
import com.ssplugins.shadow.exceptions.ShadowParseException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShadowTools {
	
	public static <T> Optional<T> get(T t) {
		return Optional.ofNullable(t);
	}
	
	public static <T> String asString(List<T> list) {
		return asString(list, 0, list.size());
	}
	
	public static <T> String asString(List<T> list, int start, int end) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			builder.append(list.get(i));
		}
		return builder.toString();
	}
	
	public static <T> List<T> lockList(List<T> list) {
		return Collections.unmodifiableList(list);
	}
	
	public static <T> void editList(List<T> list, Predicate<T> predicate, Function<T, T> function) {
		ListIterator<T> it = list.listIterator();
		while (it.hasNext()) {
			T t = it.next();
			if (predicate.test(t)) {
				it.set(function.apply(t));
			}
		}
	}
	
	public static void verify(boolean b, String msg, ParseContext context) {
		if (!b) throw new ShadowParseException(msg, context);
	}
	
	public static void verifyArgs(String[] args, int len, ParseContext context) {
		verify(args.length >= len, "Invalid argument count.", context);
	}
	
	public static void verifySections(List<ShadowSection> sections, Range range) {
		if (!range.inRange(sections.size())) throw new ShadowExecutionException("Invalid number of sections. Expected: " + range.toString() + ", Received: " + sections.size());
	}
	
	public static Optional<Boolean> asBoolean(ShadowSection section, Scope scope) {
		if (section.isReplacer()) {
			return asBoolean(getReplacerValue(section.asReplacer(), scope), scope);
		}
		else if (section.isLazyReplacer()) {
			List<ShadowSection> sections = evalLazyReplacer(section.asLazyReplacer(), scope);
			if (sections.size() == 0) return Optional.empty();
			return asBoolean(sections.get(0), scope);
		}
		else if (section.isScopeVar()) {
			return scope.getVar(section.asScopeVar().getVar())
						.map(AtomicReference::get)
						.map(Reference::new)
						.flatMap(ref -> asBoolean(ref, scope));
		}
		else if (section.isEvalGroup()) {
			return asBoolean(executeEval(section.asEvalGroup(), scope), scope);
		}
		else if (section.isExpression()) {
			return asBoolean(getExpressionValue(section.asExpression(), scope), scope);
		}
		else if (section.isEvalGroup()) {
			return asBoolean(executeEval(section.asEvalGroup(), scope), scope);
		}
		else if (section.isReference()) {
			Object o = section.asReference().getValue();
			if (o instanceof Boolean) {
				return Optional.of((Boolean) o);
			}
			else if (o instanceof String) {
				String s = (String) o;
				if (s.equalsIgnoreCase("true")) return Optional.of(true);
				else if (s.equalsIgnoreCase("false")) return Optional.of(false);
			}
		}
		else if (section.isPrimitive()) {
			return asBoolean(new Reference(section.asPrimitive().get()), scope);
		}
		return Optional.empty();
	}
	
	public static Optional<Object> asObject(ShadowSection section, Scope scope) {
		if (section.isPrimitive()) return Optional.ofNullable(section.asPrimitive().get());
		else if (section.isReference()) return Optional.ofNullable(section.asReference().getValue());
		else if (section.isReplacer()) return asObject(getReplacerValue(section.asReplacer(), scope), scope);
		else if (section.isExpression()) return asObject(getExpressionValue(section.asExpression(), scope), scope);
		else if (section.isEvalGroup()) return asObject(executeEval(section.asEvalGroup(), scope), scope);
		else if (section.isScopeVar()) return asObject(getScopeVar(section.asScopeVar(), scope), scope);
		return Optional.empty();
	}
	
	public static Optional<Object[]> asObjectArray(List<ShadowSection> sections, Scope scope) {
		try {
			Object[] arr = new Object[sections.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = asObject(sections.get(i), scope).orElseThrow(ShadowException.err("Could not convert sections to objects."));
			}
			return Optional.of(arr);
			//ShadowException.err("Could not convert sections to objects.")
		} catch (ShadowException e) {
			return Optional.empty();
		}
	}
	
	public static String sectionsToString(List<ShadowSection> list, Scope scope) {
		StringBuilder builder = new StringBuilder();
		list.forEach(section -> {
			if (section.isReplacer()) {
				builder.append(getReplacerValue(section.asReplacer(), scope).toString());
			}
			else if (section.isMultiPart()) {
				builder.append(sectionsToString(section.asMultiPart().getParts(), scope));
			}
			else if (section.isLazyReplacer()) {
				builder.append(sectionsToString(evalLazyReplacer(section.asLazyReplacer(), scope), scope));
			}
			else if (section.isExpression()) {
				builder.append(getExpressionValue(section.asExpression(), scope));
			}
			else if (section.isScopeVar()) {
				builder.append(getScopeVar(section.asScopeVar(), scope));
			}
			else builder.append(section.toString());
			builder.append(" ");
		});
		return builder.substring(0, builder.length() - 1);
	}
	
	public static ShadowSection getExpressionValue(Expression exp, Scope scope) {
		Optional<ExpressionDef> op = scope.getContext().findExpression(exp.getOperator());
		if (!op.isPresent()) throw new ShadowExecutionException("Expression not found: " + exp.getOperator());
		ExpressionDef def = op.get();
		return get(def.getAction()).map(action -> action.execute(exp.getLeft(), exp.getRight(), scope)).orElseThrow(ShadowException.err("Expression (" + exp.getOperator() + ") has no action defined."));
	}
	
	public static ShadowSection getReplacerValue(Replacer replacer, Scope scope) {
		Optional<ReplacerDef> op = scope.getContext().findReplacer(replacer.getToken());
		if (!op.isPresent()) throw new ShadowExecutionException("Replacer not found: " + replacer.getToken());
		List<ShadowSection> content = replacer.getContent();
		if (content.size() == 0) return new Empty();
		else {
			ShadowSection section = content.get(0);
			if (section.isLazyReplacer()) {
				List<ShadowSection> parsedValues = evalLazyReplacer(section.asLazyReplacer(), scope);
				editList(parsedValues, ShadowSection::isReplacer, shadowSection -> getReplacerValue(shadowSection.asReplacer(), scope));
				return get(op.get().getAction()).map(action -> action.apply(parsedValues, scope)).orElse(new Empty());
			}
			else if (section.isEvalGroup()) {
				return executeEval(section.asEvalGroup(), scope);
			}
			else if (section.isReplacer()) {
				return getReplacerValue(section.asReplacer(), scope);
			}
			else {
				return get(op.get().getAction()).map(action -> action.apply(content, scope)).orElse(new Empty());
			}
		}
	}
	
	public static List<ShadowSection> evalLazyReplacer(LazyReplacers replacers, Scope scope) {
		List<ShadowSection> list = new ArrayList<>();
		String content = replacers.getContent();
		int last = 0;
		Matcher m = Pattern.compile("(\\w+)(?:\\{(.+)})").matcher(content);
		while (m.find()) {
			String plain = content.substring(last, m.start());
			if (!plain.isEmpty()) list.add(Primitive.string(plain));
			list.add(Replacer.temp(m.group(1), m.group(2), scope));
			last = m.end();
		}
		return list;
	}
	
	public static ShadowSection executeEval(EvalGroup evalGroup, Scope scope) {
		TypeReference ref = new TypeReference();
		for (EvalSection section : evalGroup.getSections()) {
			Optional<EvalSymbolDef> op = scope.getContext().findEvalSymbol(section.getToken());
			if (!op.isPresent()) throw new ShadowExecutionException("Eval symbol not found: " + section.getToken());
			EvalSymbolDef def = op.get();
			get(def.getAction()).ifPresent(evalAction -> evalAction.execute(ref, section, scope));
			if (ref.getValue() == null && ref.getType() == null) break;
		}
		return ref.toSection();
	}
	
	public static ShadowSection getScopeVar(ScopeVar var, Scope scope) {
		Optional<NamedReference<Object>> v = scope.getVar(var.getVar());
		return v.<ShadowSection>map(ref -> new Reference(ref.get())).orElseGet(Empty::new);
	}
	
}
