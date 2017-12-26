package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.ParseContext;
import com.ssplugins.shadow2.ShadowTools;
import com.ssplugins.shadow2.element.*;
import com.ssplugins.shadow2.exceptions.ShadowParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SectionParser {
	
	List<ShadowSection> getSections(String[] sections, ParseContext context);
	
	static EvalGroup parseAsEval(String[] sections, ParseContext context) {
		List<EvalSection> list = new ArrayList<>();
		for (String s : sections) {
			Matcher m = EvalSymbolDef.PATTERN.matcher(s);
			if (m.find()) {
				String token = ShadowTools.get(m.group(1)).orElse("");
				String name = m.group(2);
				String[] args = ShadowTools.get(m.group(3)).map(s1 -> s1.split(", *")).orElse(new String[0]);
				Optional<EvalSymbolDef> op = context.getEvalSymbols().stream().filter(EvalSymbolDef.is(token)).findFirst();
				if (!op.isPresent()) throw new ShadowParseException("Unknown eval symbol: \"" + token + "\"", context);
				EvalSymbolDef def = op.get();
				List<ShadowSection> params = ShadowTools.get(def.getSectionParser()).orElse(SectionParser.standard()).getSections(args, context);
				list.add(new EvalSection(token, name, params));
			}
			else throw new ShadowParseException("Section does not match eval pattern: \"" + s + "\"", context);
		}
		return new EvalGroup(list);
	}
	
	static EvalGroup parseAsEval(String input, ParseContext context) {
		return parseAsEval(Splitter.evalSplit().split(input, context), context);
	}
	
	static SectionParser evalParser() {
		return (sections, context) -> {
			List<ShadowSection> out = new ArrayList<>();
			out.add(parseAsEval(sections, context));
			return out;
		};
	}
	
	static SectionParser replacerContents() {
		return (sections, context) -> {
			String data = ShadowTools.get(sections).filter(strings -> strings.length > 0).map(strings -> strings[0]).orElse("");
			List<ShadowSection> out = new ArrayList<>();
			if (!data.isEmpty()) out.add(new LazyReplacers(sections[0]));
			return out;
		};
	}
	
	static SectionParser allPlain() {
		return (sections, context) -> {
			return Stream.of(sections).map(Plain::new).collect(Collectors.toList());
		};
	}
	
	static SectionParser standard() {
		return (sections, context) -> {
			List<ShadowSection> out = new ArrayList<>();
			for (int i = 0; i < sections.length; i++) {
				String s = sections[i];
				Optional<ExpressionDef> op = context.findExpression(s);
				if (op.isPresent()) {
					if (i == 0 || out.get(out.size() - 1) instanceof Expression) throw new ShadowParseException("Expression has no lefthand element.", context);
					if (i + 1 == sections.length) throw new ShadowParseException("Expression has no righthand element.", context);
					ShadowSection left = out.remove(out.size() - 1);
					ShadowSection right = getSection(sections[i + 1], context);
					i++;
					out.add(new Expression(left, s, right));
				}
				else out.add(getSection(s, context));
			}
			return out;
		};
	}
	
	static ShadowSection getSection(String section, ParseContext context) {
		if (section.startsWith("\"") && section.endsWith("\"")) {
			return new Plain(section.substring(1, section.length() - 1).replace("\\\"", "\""));
		}
		else if (section.contains("{")) {
			int b = section.indexOf('{');
			String token = section.substring(0, b);
			String content = section.substring(b + 1, section.lastIndexOf('}'));
			Optional<ReplacerDef> op = context.getReplacers().stream().filter(ReplacerDef.is(token)).findFirst();
			if (!op.isPresent()) throw new ShadowParseException("Unknown replacer: " + token, context);
			ReplacerDef rDef = op.get();
			Splitter splitter = ShadowTools.get(rDef.getSplitter()).orElse(Splitter.replacerSplit());
			SectionParser parser = ShadowTools.get(rDef.getSectionParser()).orElse(SectionParser.replacerContents());
			return new Replacer(token, parser.getSections(splitter.split(content, context), context));
		}
		else {
			Plain p = new Plain(section);
			Optional<Number> nop = ShadowTools.asNumber(p);
			return nop.<ShadowSection>map(Reference::new).orElse(p);
		}
	}
	
}
