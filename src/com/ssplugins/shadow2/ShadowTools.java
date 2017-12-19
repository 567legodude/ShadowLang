package com.ssplugins.shadow2;

import com.ssplugins.shadow2.common.TypeReference;
import com.ssplugins.shadow2.def.EvalSymbolDef;
import com.ssplugins.shadow2.def.ReplacerDef;
import com.ssplugins.shadow2.element.*;
import com.ssplugins.shadow2.exceptions.ShadowExecutionException;

import java.util.*;
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
	
	public static Optional<Number> asNumber(Plain plain) {
		String s = plain.toString();
		try {
			if (s.contains(".")) {
				return Optional.of(Double.valueOf(s));
			}
			else {
				return Optional.of(Integer.valueOf(s));
			}
		} catch (NumberFormatException e) {
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
			else builder.append(section.toString());
			builder.append(" ");
		});
		return builder.substring(0, builder.length() - 1);
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
			if (!plain.isEmpty()) list.add(new Plain(plain));
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
			get(def.getAction()).ifPresent(evalAction -> evalAction.execute(ref, section));
		}
		return ref.toSection();
	}
	
}
