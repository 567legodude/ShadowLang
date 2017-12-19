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
			for (String s : sections) {
				if (s.contains("{")) {
					int b = s.indexOf('{');
					String token = s.substring(0, b);
					String content = s.substring(b + 1, s.lastIndexOf('}'));
					Optional<ReplacerDef> op = context.getReplacers().stream().filter(ReplacerDef.is(token)).findFirst();
					if (!op.isPresent()) throw new ShadowParseException("Unknown replacer: " + token, context);
					ReplacerDef rDef = op.get();
					Splitter splitter = ShadowTools.get(rDef.getSplitter()).orElse(Splitter.replacerSplit());
					SectionParser parser = ShadowTools.get(rDef.getSectionParser()).orElse(SectionParser.replacerContents());
					Replacer replacer = new Replacer(token, parser.getSections(splitter.split(content, context), context));
					out.add(replacer);
				}
				else {
					out.add(new Plain(s));
				}
			}
			return out;
		};
	}
	
}
