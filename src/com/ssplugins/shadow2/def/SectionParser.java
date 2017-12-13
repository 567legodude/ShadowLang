package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.ParseContext;
import com.ssplugins.shadow2.ShadowTools;
import com.ssplugins.shadow2.element.LazyReplacers;
import com.ssplugins.shadow2.element.Plain;
import com.ssplugins.shadow2.element.Replacer;
import com.ssplugins.shadow2.element.ShadowSection;
import com.ssplugins.shadow2.exceptions.ShadowParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface SectionParser {
	
	List<ShadowSection> getSections(String[] sections, ParseContext context);
	
	static SectionParser evalParser() {
		return (sections, context) -> {
		
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
	
	static SectionParser standard() {
		return (sections, context) -> {
			List<ShadowSection> list = new ArrayList<>();
			for (String s : sections) {
				if (s.contains("{")) {
					int b = s.indexOf('{');
					String token = s.substring(0, b);
					String content = s.substring(b + 1, s.lastIndexOf('}'));
					Optional<ReplacerDef> op = context.getReplacers().stream().filter(ReplacerDef.is(token)).findFirst();
					if (!op.isPresent()) throw new ShadowParseException("Unknown replacer: " + token, context.getLine());
					ReplacerDef rDef = op.get();
					Splitter splitter = ShadowTools.get(rDef.getSplitter()).orElse(Splitter.replacerSplit());
					SectionParser parser = ShadowTools.get(rDef.getSectionParser()).orElse(SectionParser.replacerContents());
					Replacer replacer = new Replacer(token, parser.getSections(splitter.split(content, context), context));
					list.add(replacer);
				}
				else {
					list.add(new Plain(s));
				}
			}
			return list;
		};
	}
	
}
