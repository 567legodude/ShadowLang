package com.ssplugins.shadow.element;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.ShadowTools;
import com.ssplugins.shadow.def.ReplacerDef;
import com.ssplugins.shadow.def.SectionParser;
import com.ssplugins.shadow.def.Splitter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Replacer extends ShadowSection {
	
	private String token;
	private List<ShadowSection> content;
	
	public Replacer(String token, List<ShadowSection> content) {
		this.token = token;
		this.content = ShadowTools.lockList(content);
	}
	
	public static Replacer temp(String token, String content, Scope scope) {
		Optional<ReplacerDef> op = scope.getContext().findReplacer(token);
		if (!op.isPresent()) {
			return new Replacer(token, Collections.singletonList(new LazyReplacers(content)));
		}
		else {
			ReplacerDef def = op.get();
			Splitter splitter = ShadowTools.get(def.getSplitter()).orElse(Splitter.replacerSplit());
			SectionParser parser = ShadowTools.get(def.getSectionParser()).orElse(SectionParser.replacerContents());
			return new Replacer(token, parser.getSections(splitter.split(content, scope.getContext()), scope.getContext()));
		}
	}
	
	public String getToken() {
		return token;
	}
	
	public List<ShadowSection> getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return token + "{" + ShadowTools.asString(content) + "}";
	}
	
}
